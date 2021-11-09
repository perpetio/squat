package com.perpetio.squat.challenge.camera

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.SurfaceView
import com.perpetio.squat.challenge.util.VisualizationUtils
import com.perpetio.squat.challenge.util.VisualizationUtils.MODEL_HEIGHT
import com.perpetio.squat.challenge.util.VisualizationUtils.MODEL_WIDTH
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class CameraSource(
    private val surfaceView: SurfaceView,
    private val cameraListener: CameraSourceListener
) {

    /** The [android.util.Size] of camera preview.  */
    private var previewSize: Size? = null

    /** A shape for extracting frame data.   */
    private val PREVIEW_WIDTH = 640
    private val PREVIEW_HEIGHT = 480

    /** An [ImageReader] that handles preview frame capture.   */
    private var imageReader: ImageReader? = null

    /** Orientation of the camera sensor.   */
    private var sensorOrientation: Int? = null

    /** The [android.util.Size.getWidth] of camera preview. */
    private var previewWidth = 0

    /** The [android.util.Size.getHeight] of camera preview.  */
    private var previewHeight = 0

    /** An IntArray to save image data in ARGB8888 format  */
    private lateinit var rgbBytes: IntArray

    /** Whether the current camera device supports Flash or not.    */
    private var flashSupported = false

    /** ID of the current [CameraDevice].   */
    private var cameraId: String? = null

    /** A [Semaphore] to prevent the app from exiting before closing the camera.    */
    private val cameraOpenCloseLock = Semaphore(1)

    /** A [Handler] for running tasks in the background.    */
    private var backgroundHandler: Handler? = null

    /** A reference to the opened [CameraDevice].    */
    private var cameraDevice: CameraDevice? = null

    /** [CaptureRequest.Builder] for the camera preview   */
    private var previewRequestBuilder: CaptureRequest.Builder? = null

    /** A [CameraCaptureSession] for camera preview.   */
    private var captureSession: CameraCaptureSession? = null

    /** [CaptureRequest] generated by [.previewRequestBuilder   */
    private var previewRequest: CaptureRequest? = null

    /** A ByteArray to save image data in YUV format  */
    private var yuvBytes = arrayOfNulls<ByteArray>(3)

    /** An additional thread for running tasks that shouldn't block the UI.   */
    private var backgroundThread: HandlerThread? = null

    private val cameraManager: CameraManager by lazy {
        val context = surfaceView.context
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    /** [CameraDevice.StateCallback] is called when [CameraDevice] changes its state.   */
    private val stateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            this@CameraSource.cameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@CameraSource.cameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            onDisconnected(cameraDevice)
        }
    }

    /**
     * A [CameraCaptureSession.CaptureCallback] that handles events related to JPEG capture.
     */
    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
        }
    }

    fun closeCamera() {
        if (captureSession == null) {
            return
        }

        try {
            cameraOpenCloseLock.acquire()
            captureSession!!.close()
            captureSession = null
            cameraDevice!!.close()
            cameraDevice = null
            imageReader!!.close()
            imageReader = null
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    @SuppressLint("MissingPermission")
    fun setUpCamera() {
        setUpCameraOutputs()
        try {
            // Wait for camera to open - 2.5 seconds is sufficient
            if (!cameraOpenCloseLock.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            cameraManager.openCamera(cameraId!!, stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(this::class.java.simpleName, e.toString())
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    /**
     * Sets up member variables related to camera.
     */
    private fun setUpCameraOutputs() {
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)

                // We don't use a back facing camera in this sample.
                val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (cameraDirection != null &&
                    cameraDirection == CameraCharacteristics.LENS_FACING_BACK
                ) {
                    continue
                }

                previewSize = Size(PREVIEW_WIDTH, PREVIEW_HEIGHT)

                imageReader = ImageReader.newInstance(
                    PREVIEW_WIDTH, PREVIEW_HEIGHT,
                    ImageFormat.YUV_420_888, /*maxImages*/ 2
                )

                sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!

                previewHeight = previewSize!!.height
                previewWidth = previewSize!!.width

                setVideoSize()

                // Initialize the storage bitmaps once when the resolution is known.
                rgbBytes = IntArray(previewWidth * previewHeight)

                // Check if the flash is supported.
                flashSupported =
                    characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                this.cameraId = cameraId

                // We've found a viable camera and finished setting up member variables,
                // so we don't need to iterate through other available cameras.
                return
            }
        } catch (e: CameraAccessException) {
            Log.e(this::class.java.simpleName, e.toString())
        } catch (e: NullPointerException) {
            cameraListener.onConfigureCameraError()
        }
    }

    private fun setVideoSize() {
        val screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels
        surfaceView.holder!!.setFixedSize(screenWidth, screenWidth)
    }

    private fun createCameraPreviewSession() {
        try {

            // We capture images from preview in YUV format.
            imageReader = ImageReader.newInstance(
                previewSize!!.width, previewSize!!.height, ImageFormat.YUV_420_888, 2
            )
            imageReader!!.setOnImageAvailableListener(imageAvailableListener, backgroundHandler)

            // This is the surface we need to record images for processing.
            val recordingSurface = imageReader!!.surface

            // We set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder = cameraDevice!!.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            )
            previewRequestBuilder!!.addTarget(recordingSurface)

            // Here, we create a CameraCaptureSession for camera preview.

            cameraDevice!!.createCaptureSession(
                listOf(recordingSurface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        // The camera is already closed
                        if (cameraDevice == null) return

                        // When the session is ready, we start displaying the preview.
                        captureSession = cameraCaptureSession
                        try {
                            // Auto focus should be continuous for camera preview.
                            previewRequestBuilder!!.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                            )
                            // Flash is automatically enabled when necessary.
                            setAutoFlash(previewRequestBuilder!!)

                            // Finally, we start displaying the camera preview.
                            previewRequest = previewRequestBuilder!!.build()
                            captureSession!!.setRepeatingRequest(
                                previewRequest!!,
                                captureCallback, backgroundHandler
                            )
                        } catch (e: CameraAccessException) {
                            Log.e(this::class.java.simpleName, e.toString())
                        }
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                        cameraListener.onConfigureCameraError()
                    }
                },
                null
            )
        } catch (e: CameraAccessException) {
            Log.e(this::class.java.simpleName, e.toString())
        }
    }

    /** A [OnImageAvailableListener] to receive frames as they are available.  */
    private var imageAvailableListener = object : OnImageAvailableListener {
        override fun onImageAvailable(imageReader: ImageReader) {
            // We need wait until we have some size from onPreviewSizeChosen
            if (previewWidth == 0 || previewHeight == 0) {
                return
            }

            val image = imageReader.acquireLatestImage() ?: return
            fillBytes(image.planes, yuvBytes)

            VisualizationUtils.convertYUV420ToARGB8888(
                yuvBytes[0]!!,
                yuvBytes[1]!!,
                yuvBytes[2]!!,
                previewWidth,
                previewHeight,
                image.planes[0].rowStride,
                image.planes[1].rowStride,
                image.planes[1].pixelStride,
                rgbBytes
            )

            // Create bitmap from int array
            val imageBitmap = Bitmap.createBitmap(
                rgbBytes, previewWidth, previewHeight,
                Bitmap.Config.ARGB_8888
            )

            // Create rotated version for portrait display
            val rotateMatrix = Matrix()
            rotateMatrix.postRotate(270.0f)
            rotateMatrix.postScale(-1f, 1f) // Mirror

            val rotatedBitmap = Bitmap.createBitmap(
                imageBitmap, 0, 0, previewWidth, previewHeight,
                rotateMatrix, true
            )
            image.close()

            processImage(rotatedBitmap)
        }
    }

    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
        if (flashSupported) {
            requestBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
        }
    }

    /** Process image using Posenet library.   */
    private fun processImage(bitmap: Bitmap) {
        // Crop bitmap.
        val croppedBitmap = cropBitmap(bitmap)
        // Created scaled version of bitmap for model input.
        val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, MODEL_WIDTH, MODEL_HEIGHT, true)
        cameraListener.onCalculate(scaledBitmap)
    }

    /** Crop Bitmap to maintain aspect ratio of model input.   */
    private fun cropBitmap(bitmap: Bitmap): Bitmap {
        val bitmapRatio = bitmap.height.toFloat() / bitmap.width
        val modelInputRatio = MODEL_HEIGHT.toFloat() / MODEL_WIDTH
        var croppedBitmap = bitmap

        // Acceptable difference between the modelInputRatio and bitmapRatio to skip cropping.
        val maxDifference = 1e-5

        // Checks if the bitmap has similar aspect ratio as the required model input.
        when {
            abs(modelInputRatio - bitmapRatio) < maxDifference -> return croppedBitmap
            modelInputRatio < bitmapRatio -> {
                // New image is taller so we are height constrained.
                val cropHeight = bitmap.height - (bitmap.width.toFloat() / modelInputRatio)
                croppedBitmap = Bitmap.createBitmap(
                    bitmap,
                    0,
                    (cropHeight / 2).toInt(),
                    bitmap.width,
                    (bitmap.height - cropHeight).toInt()
                )
            }
            else -> {
                val cropWidth = bitmap.width - (bitmap.height.toFloat() * modelInputRatio)
                croppedBitmap = Bitmap.createBitmap(
                    bitmap,
                    (cropWidth / 2).toInt(),
                    0,
                    (bitmap.width - cropWidth).toInt(),
                    bitmap.height
                )
            }
        }
        return croppedBitmap
    }

    /** Fill the yuvBytes with data from image planes.   */
    private fun fillBytes(planes: Array<Image.Plane>, yuvBytes: Array<ByteArray?>) {
        // Row stride is the total number of bytes occupied in memory by a row of an image.
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (i in planes.indices) {
            val buffer = planes[i].buffer
            if (yuvBytes[i] == null) {
                yuvBytes[i] = ByteArray(buffer.capacity())
            }
            buffer.get(yuvBytes[i]!!)
        }
    }

    fun startBackgroundThread() {
        backgroundThread = HandlerThread("imageAvailableListener").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            Log.e(this::class.java.simpleName, e.toString())
        }
    }

    interface CameraSourceListener {
        fun onCalculate(bitmap: Bitmap)
        fun onConfigureCameraError()
    }

}