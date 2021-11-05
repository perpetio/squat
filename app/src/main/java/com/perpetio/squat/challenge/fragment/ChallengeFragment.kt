package com.perpetio.squat.challenge.fragment

import android.Manifest
import android.graphics.*
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.miguelrochefort.fitnesscamera.lib.BodyPart
import com.miguelrochefort.fitnesscamera.lib.Person
import com.miguelrochefort.fitnesscamera.lib.Posenet
import com.perpetio.squat.challenge.R
import com.perpetio.squat.challenge.camera.CameraSource
import com.perpetio.squat.challenge.databinding.FragmentChallengeBinding
import com.perpetio.squat.challenge.dialog.ErrorDialog
import com.perpetio.squat.challenge.model.SquatViewModel
import com.perpetio.squat.challenge.ChallengeRepetitionCounter
import com.perpetio.squat.challenge.util.VisualizationUtils.MODEL_HEIGHT
import com.perpetio.squat.challenge.util.VisualizationUtils.MODEL_WIDTH

class ChallengeFragment : Fragment() {

    private val countingToStartInMiliseconds: Long = 1000
    private val exercisesTimeInMiliseconds: Long = 10000

    private var binding: FragmentChallengeBinding? = null

    private var counter: ChallengeRepetitionCounter? = null

    private lateinit var posenet: Posenet

    private var surfaceView: SurfaceView? = null

    private val sharedViewModel: SquatViewModel by activityViewModels()
    private var camera: CameraSource? = null

    private var paint = Paint()

    private val minConfidence = 0.5

    private val circleRadius = 16.0f
    private val smallCircleRadius = 8.0f
    private var previewMode = 0
    private var startTimerCount: Int = 0
    private var textToSpeech: TextToSpeech? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentChallengeBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val startStopBtn = binding?.startBtn
        surfaceView = binding?.surfaceView
        textToSpeech = TextToSpeech(requireContext()) {}
        counter = ChallengeRepetitionCounter(
            sharedViewModel.type.value,
            object : ChallengeRepetitionCounter.CounterListener {
                override fun onCounterVoice(quantity: Int) {
                    voiceTheNumber(quantity)
                }
            })
        startStopBtn?.setOnClickListener {
            startStopBtn.isActivated = !startStopBtn.isActivated
            val changeTurnState = counter!!.isTurnOn
            counter!!.changeTurnState()
            if (!changeTurnState) {
                counter!!.resetCounter()
                timer.start()
            } else {
                startTimerCount = 0
                timer.cancel();
                binding?.counter?.text = resources.getText(R.string.challenge_fragment_default_time)
                binding?.progress?.progress = 0
            }
        }
    }

    override fun onStart() {
        super.onStart()
        camera = CameraSource(surfaceView!!, object : CameraSource.CameraSourceListener {
            override fun onCalculate(bitmap: Bitmap) {
                var person = posenet.estimateSinglePose(bitmap)
                person = swapBodyParts(person)
                val canvas: Canvas = surfaceView?.holder?.lockCanvas()!!
                draw(canvas, person, bitmap)
                val count = counter!!.onCalculateData(person)
                activity?.runOnUiThread { sharedViewModel.setScore(count) }
            }

            override fun onConfigureCameraError() {
                activity?.runOnUiThread {
                    Toast.makeText(
                        requireContext(), "Camera on configure failed", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
        openCamera()
        posenet = Posenet(requireContext())
    }

    override fun onResume() {
        super.onResume()
        camera?.startBackgroundThread()
    }

    override fun onPause() {
        camera?.closeCamera()
        camera?.stopBackgroundThread()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        timer.cancel();
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        posenet.close()
    }

    private val timer = object : CountDownTimer(countingToStartInMiliseconds + exercisesTimeInMiliseconds, 1000) {
        override fun onFinish() {
            startTimerCount = 0
            counter!!.changeTurnState()
            findNavController().navigate(R.id.action_challengeFragment_to_resultFragment)
            sharedViewModel.addScoreToLeaderBoardList()
        }

        override fun onTick(tickTime: Long) {
            val countingToStartInSecond = countingToStartInMiliseconds / 1000
            if (startTimerCount < countingToStartInSecond) {
                binding?.startCounter?.visibility = View.VISIBLE
                val countingToStart = (countingToStartInSecond - startTimerCount).toInt()
                binding?.startCounter?.text = countingToStart.toString()
                voiceTheNumber(countingToStart)
                startTimerCount++
            } else {
                binding?.startCounter?.visibility = View.GONE
                val timeLast = tickTime / 1000
                binding?.progress?.progress = (60 - timeLast).toInt()
                binding?.counter?.text = "00 : ${if (timeLast > 9) timeLast else "0$timeLast"}"
            }
        }
    }

    private fun openCamera() {
        val appPerms = arrayOf(
            Manifest.permission.CAMERA
        )
        activityResultLauncher.launch(appPerms)
    }

    private var activityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allAreGranted = true
            for (b in result.values) {
                allAreGranted = allAreGranted && b
            }
            if (allAreGranted) {
                camera?.setUpCamera()
            } else {
                activity?.supportFragmentManager?.let {
                    ErrorDialog.newInstance(getString(R.string.tfe_pn_request_permission))
                        .show(it, "camera dialog")
                }
            }
        }

    private fun draw(canvas: Canvas, person: Person, bitmap: Bitmap) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        val screenWidth: Int
        val screenHeight: Int
        val left: Int
        val top: Int
        if (canvas.height > canvas.width) {
            screenWidth = canvas.width
            screenHeight = canvas.width
            left = 0
            top = (canvas.height - canvas.width) / 2
        } else {
            screenWidth = canvas.height
            screenHeight = canvas.height
            left = (canvas.width - canvas.height) / 2
            top = 0
        }
        val right: Int = left + screenWidth
        val bottom: Int = top + screenHeight

        setPaint()
        if (previewMode == 0 || previewMode == 2) {
            canvas.drawBitmap(
                bitmap,
                Rect(0, 0, bitmap.width, bitmap.height),
                Rect(left, top, right, bottom),
                paint
            )
        }

        val widthRatio = screenWidth.toFloat() / MODEL_WIDTH
        val heightRatio = screenHeight.toFloat() / MODEL_HEIGHT

        if (previewMode == 0 || previewMode == 1) {
            for (keyPoint in person.keyPoints) {
                if (keyPoint.score > minConfidence) {
                    val position = keyPoint.position
                    val adjustedX: Float = position.x.toFloat() * widthRatio + left
                    val adjustedY: Float = position.y.toFloat() * heightRatio + top
                    var radius =
                        if (keyPoint.bodyPart.ordinal < 5) smallCircleRadius else circleRadius // nose, eyes, ears
                    if (keyPoint.bodyPart == BodyPart.NOSE) {
                        radius = 60.0f
                    }
                    canvas.drawCircle(adjustedX, adjustedY, radius, paint)
                }
            }

            for (line in bodyJoints) {
                if (
                    (person.keyPoints[line.first.ordinal].score > minConfidence) and
                    (person.keyPoints[line.second.ordinal].score > minConfidence)
                ) {
                    canvas.drawLine(
                        person.keyPoints[line.first.ordinal].position.x.toFloat() * widthRatio + left,
                        person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio + top,
                        person.keyPoints[line.second.ordinal].position.x.toFloat() * widthRatio + left,
                        person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio + top,
                        paint
                    )
                }
            }
        }

        surfaceView?.holder!!.unlockCanvasAndPost(canvas)
    }

    /** List of body joints that should be connected.    */
    private val bodyJoints = listOf(
        Pair(BodyPart.LEFT_WRIST, BodyPart.LEFT_ELBOW),
        Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
        Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
        Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
        Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
        Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
        Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE),
        Pair(BodyPart.MID_HIP, BodyPart.MID_SHOULDER)
    )


    // To prevent horizontal miscategorization of body parts,
    // and assuming that the user is always facing the camera,
    // we make sure that body parts are on the right side of the body.
    private fun swapBodyParts(person: Person): Person {
        swap(person, BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER);
        swap(person, BodyPart.LEFT_ANKLE, BodyPart.RIGHT_ANKLE);
        swap(person, BodyPart.LEFT_EAR, BodyPart.RIGHT_EAR);
        swap(person, BodyPart.LEFT_ELBOW, BodyPart.RIGHT_ELBOW);
        swap(person, BodyPart.LEFT_EYE, BodyPart.RIGHT_EYE);
        swap(person, BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP);
        swap(person, BodyPart.LEFT_KNEE, BodyPart.RIGHT_KNEE);
        swap(person, BodyPart.LEFT_WRIST, BodyPart.RIGHT_WRIST);

        return person;
    }

    private fun swap(person: Person, left: BodyPart, right: BodyPart) {
        if (person.keyPoints[right.ordinal].score >= minConfidence
            && person.keyPoints[left.ordinal].score >= minConfidence
            && person.keyPoints[right.ordinal].position.x > person.keyPoints[left.ordinal].position.x
        ) {
            val temp = person.keyPoints[right.ordinal]
            person.keyPoints[right.ordinal] = person.keyPoints[left.ordinal]
            person.keyPoints[left.ordinal] = temp
        }
    }

    private fun setPaint() {
        paint.color = Color.WHITE
        paint.alpha = 128
        paint.textSize = 80.0f
        paint.strokeWidth = 8.0f
    }

    private fun voiceTheNumber(quantity: Int) {
        textToSpeech?.speak(quantity.toString(), TextToSpeech.QUEUE_FLUSH, null, null)
    }
}