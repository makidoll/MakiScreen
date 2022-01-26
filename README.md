# MakiScreen
> 📺 Streaming OBS video into maps on item frames at a high frame rate

https://user-images.githubusercontent.com/85983303/145347904-9933197b-7d95-47ef-9a8b-be37b285ae10.mp4

## How does it work

- Load jar plugin onto 1.18.x Paper server
- It will start a thread to listen for UDP packets on port 1337
- Use ffmpeg to send [OBS Virtual Camera](https://www.youtube.com/watch?v=bfrknjDzukI) as JPG frames to the UDP socket
- Renders the latest available frame in Minecraft! 

## Get started (It's not easy, and it's experimental)

**Beware that map will probably be overwritten**

- Download [MakiScreen](https://github.com/makitsune/MakiScreen/actions) jar **not** the `Original-` and place it in the server plugins folder
- Download [FFmpeg](http://ffmpeg.org/download.html) and make sure it's in your path (or add it to your PATH environment variable)
- Download [OBS](https://obsproject.com)
- Run **Paper** server and make sure port **1337 UDP** is available, and then turn it off after the server has finished starting up
- Change the ***size*** setting in the config.yml and match your **output** resolution in OBS to your `size:` setting in the config.yml
```yaml
# Set your OBS output resolution according to this option below.
# 1 = 256x128 = 2 maps
# 2 = 512x256 = 8 maps
# 3 = 1024x512 = 32 maps
size: 2
```
- Run **OBS** and make sure the output resolution is according to the config option and the base resolution is set to same as the config or any resolution with 2:1 aspect ratio, and set the frame rate to **20**
- Turn on your ***OBS Virtual Camera***
- Open terminal shell and enter `ffmpeg -y -f dshow -thread_queue_size 4096 -hwaccel cuda -hwaccel_output_format cuda -i video="OBS Virtual Camera" -f rawvideo -c:v mjpeg -qscale:v 16 -r 20 udp://127.0.0.1:1337`
  - you can remove `-hwaccel cuda -hwaccel_output_format cuda` if you're not using nvidia GPU
  - set `-qscale:v 1` you can increase this value from 1 to 31 to lower your bitrate
- Run **Paper** server and Type `/maki` in Minecraft to get the maps

if your performance went doodoo, you could try removing both data.yml in the MakiScreen folder and removing anything in the data folder in the world folder

## Help me

You can contact me on **Discord** at [Maki#4845](https://maki.cat/discord) or on **Twitter** at [@MakiXx_](https://twitter.com/MakiXx_)

## Credit
- [CodedRed](https://www.youtube.com/channel/UC_kPUW3XPrCCRT9a4Pnf1Tg) For ImageManager class
- [DNx5](https://github.com/dnx5) for synchronizing the maps, optimizing the code, implementing sierra2 dithering. literally do all the hard work for me
- [EzMediaCore](https://github.com/MinecraftMediaLibrary/EzMediaCore) for the dither algorithm