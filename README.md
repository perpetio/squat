# Sports Challenges

## Introduction

### Sports Challenges is a fitness app that counts the number of exercises performed based on image recognition.

The application supports Android 5.0 Lollipop (API level 21) and above.

## See our tutorial post:
[How to Build an Image Recognition App with AI and Machine Learning](https://perpet.io/blog/how-to-build-an-image-recognition-app-a-step-by-step-tutorial-and-use-cases-examples/)

### Features include:
* The current version of app support next challenges :
  - Squatting
  - Jumping
* An easy-to-use interface
* Native Android app

### Used libraries :
* [Navigation Architecture Component](<https://developer.android.com/guide/navigation/navigation-getting-started>) - Navigation is a framework for navigating between 'destinations' within an Android application that provides a consistent API whether destinations are implemented as Fragments, Activities, or other components.
* [Dependency injection with Hilt](<https://developer.android.com/training/dependency-injection/hilt-android>) - Hilt is a dependency injection library for Android that reduces the boilerplate of doing manual dependency injection in your project. 
* [Firebase Realtime Database](<https://firebase.google.com/docs/database>) - Store and sync data with our NoSQL cloud database. Data is synced across all clients in realtime, and remains available when your app goes offline.

### Building

#### With Android Studio

The easiest way to build is to install [Android Studio](https://developer.android.com/sdk/index.html) with Gradle. Once installed, then you can import the project into Android Studio:

1. Open `File`
2. Import Project
3. Select `build.gradle` under the project directory
4. Click `OK`

Then, Gradle will do everything for you.

#### With Gradle

This project requires the [Android SDK](http://developer.android.com/sdk/index.html) to be installed in your development environment. In addition you'll need to set the `ANDROID_HOME` environment variable to the location of your SDK. For example:

`export ANDROID_HOME=/home/<user>/tools/android-sdk`

After satisfying those requirements, the build is pretty simple:

- Run `./gradlew build` from the within the project folder. It will build the project for you and install it to the connected Android device or running emulator.

## Links:
- MediaPipe https://google.github.io/mediapipe/solutions/pose.html
- Machine learning for mobile developers https://developers.google.com/ml-kit
