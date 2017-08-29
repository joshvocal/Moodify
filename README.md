# Moodify

An android app that plays songs from Spotify based on your current mood from a selfie.

<img src="https://github.com/joshvocal/Moodify/blob/master/screenshots/screenshot_1.png" align="right" hspace="20">

![](https://github.com/joshvocal/Moodify/blob/master/screenshots/screenshot_4.png)

![](https://github.com/joshvocal/Moodify/blob/master/screenshots/screenshot_2.png)




## Demo

<img src="https://github.com/joshvocal/Moodify/blob/master/screenshots/happiness.gif" align="right" hspace="20">

![](https://github.com/joshvocal/Moodify/blob/master/screenshots/sadness.gif)

## Screenshots

![](https://github.com/joshvocal/Moodify/blob/master/screenshots/screenshot_2.png)

![](https://github.com/joshvocal/Moodify/blob/master/screenshots/screenshot_3.png)


## Getting Started

### Prerequisitesd

* A Premium Spotify account

```
Only Premium Spotify users will be able to log in and play music with this library.
```

* A Microsoft Azure Emotion API key

```
https://azure.microsoft.com/en-ca/try/cognitive-services/
```

* A Spotify Android SDK key

```
https://developer.spotify.com/
```

### Installation

Navigate to res/values/strings in the project

```
Replace emotion_api_key, spotify_client_id and spotify_redirect_uri with yours.
```


## Built With

* [Microsoft Azure Emotion API](https://azure.microsoft.com/en-us/services/cognitive-services/emotion/) - Used to detect emotions.
* [Spotify Android SDK](https://github.com/spotify/android-sdk) - Used to control Spotify songs.
* [kaaes' Spotify Web API for Android](https://github.com/kaaes/spotify-web-api-android) - Used to fetch Spotify songs.
* [Picasso Library](https://square.github.io/picasso/) - Used to fetch album artwork from Spotify songs.


