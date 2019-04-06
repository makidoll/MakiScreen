# MakiScreen
> 📺 Streaming OBS video into maps on item frames at a high frame rate

# How does it works

- Load jar plugin onto 1.13+ Spigot server
- It will start a thread to listen for UDP packets on port 1337
- Use ffmpeg to send [OBS-VirtualCamera](https://obsproject.com/forum/resources/obs-virtualcam.539) as JPG frames to the UDP socket
- Renders the latest available frame in Minecraft! 

# Get started (It's not easy and it's experimental)

**Beware that map ID 0 and 1 will probably be overwritten**

- Download [MakiScreen.jar]() and place in Spigot 1.13+ server
- Download [FFmpeg](http://ffmpeg.org/download.html) and make sure its in your path
- Download [OBS](https://obsproject.com)
- Download [OBS-VirtualCamera](https://obsproject.com/forum/resources/obs-virtualcam.539)
- Run **Spigot** server and make sure port **1337 UDP** is available
- Run **OBS** and make sure the output resolution is **256x128**
- Enable **Tools>VirtualCam**
- Open terminal shell and enter `ffmpeg -y -f dshow -i video="OBS-Camera" -vf scale=256:128 -f rawvideo -c:v mjpeg -qscale:v 1 -r 60 udp://127.0.0.1:1337`

# Help

You can contact me on **Discord** at **Maki#4845** or on **Twitter** at [@MakiXx_](https://twitter.com/MakiXx_)
