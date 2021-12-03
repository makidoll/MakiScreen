# MakiScreen
> 📺 Streaming OBS video into maps on item frames at a high frame rate

[![](youtube-embed.png)](https://youtu.be/IvEZr8z5eu4)

## How does it work

- Load jar plugin onto 1.17.1 Paper server
- It will start a thread to listen for UDP packets on port 1337
- Use ffmpeg to send [OBS Virtual Camera](https://www.youtube.com/watch?v=bfrknjDzukI) as JPG frames to the UDP socket
- Renders the latest available frame in Minecraft! 

## Get started (It's not easy, and it's experimental)

- Download [MakiScreen](https://github.com/makitsune/MakiScreen/releases/tag/1.0) jar and place in Spigot 1.13+ server plugins
- Download [FFmpeg](http://ffmpeg.org/download.html) and make sure its in your path
- Download [OBS](https://obsproject.com)
- Run **Paper** server and make sure port **1337 UDP** is available
- Run **OBS** and make sure the output resolution is **512x256** and the base resolution is set to ***1440x720*** or any resolution with 2:1 aspect ratio, and set the frame rate to **20**
- Turn on your ***OBS Virtual Camera***
- Open terminal shell and enter `ffmpeg -y -f dshow -i video="OBS Virtual Camera" -i "Map_colors_paletteuse.png" -filter_complex "paletteuse" -f rawvideo -c:v mjpeg -qscale:v 8 -r 20 udp://127.0.0.1:1337`
- Type `/maki` in Minecraft to get the maps

## Help me

You can contact me on **Discord** at [Maki#4845](https://maki.cat/discord) or on **Twitter** at [@MakiXx_](https://twitter.com/MakiXx_)

## Credit
- [CodedRed](https://www.youtube.com/channel/UC_kPUW3XPrCCRT9a4Pnf1Tg) For ImageManager class
- [DNx5](https://github.com/dnx5) for synchronizing the maps and optimizing the code