# MakiScreen

> 📺 Streaming OBS video into maps on item frames at a high frame rate

[![](youtube-embed.png)](https://youtu.be/kyyuanJ6Pt8)

## Disclaimer

**I'm not providing support for this, please stop asking me for help.**

**This was just an experiment and I haven't touched it since since 2020**

[@mbahmodin](https://github.com/mbahmodin) created a whole bunch of changes available at [branch/mbahmodin](https://github.com/makifoxgirl/MakiScreen/tree/mbahmodin)

## How does it works

-   Load jar plugin onto 1.13+ Spigot server
-   It will start a thread to listen for UDP packets on port 1337
-   Use ffmpeg to send [OBS-VirtualCamera](https://obsproject.com/forum/resources/obs-virtualcam.539) as JPG frames to the UDP socket
-   Renders the latest available frame in Minecraft!

## Get started

**Beware that map ID 0 and 1 will probably be overwritten**

-   Download [MakiScreen](https://github.com/makitsune/MakiScreen/releases/tag/1.0) jar and place in Spigot 1.13+ server plugins
-   Download [FFmpeg](http://ffmpeg.org/download.html) and make sure its in your path
-   Download [OBS](https://obsproject.com)
-   Download [OBS-VirtualCamera](https://obsproject.com/forum/resources/obs-virtualcam.539)
-   Run **Spigot** server and make sure port **1337 UDP** is available
-   Run **OBS** and make sure the output resolution is **256x128**
-   Enable **Tools>VirtualCam**
-   Open terminal shell and enter `ffmpeg -y -f dshow -i video="OBS-Camera" -vf scale=256:128 -f rawvideo -c:v mjpeg -qscale:v 1 -r 60 udp://127.0.0.1:1337`
-   Type `/maki` in Minecraft to get both maps
