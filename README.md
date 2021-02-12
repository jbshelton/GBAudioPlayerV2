# GBAudioPlayerV2
Version 2\.0 of the incredible Gameboy Audio Player, now with a GUI and some more features\! **\(\+Patch 2\.1\)**

#### What is GBAudioPlayerV2?
I made significant improvements to both the original (Version 1\.0) source assembly code and the Java encoder program, as well as shoving in a brand\-new easy\-to\-use graphical interface for all of it\! Listen to music on \(almost\) any Gameboy\!

---

#### Improvements/fixes to past issues
- Not \(completely\) dependent on the command line anymore
- You don't have to use Audacity or a hex editor to put the audio in the ROM file \(though I recommend putting it through Audacity at least to amplify it, since I don't know how to do it in FFMPEG effectively \(yet\)\)
- Audio encodes directly into the ROM file to minimize encoding time

---

#### Changes in patch 2\.1 \(notable and important\)
- Fixed legacy encoding for GB and GBC
- Added HQ support for original GB
- Some optimization all around

---

#### Features
- One single interface to rule them all\!
- Selection of various sample rates
- Configuration for both the original Gameboy and Gameboy Color
- Select from stereo/mono, and auto mono configuration for input audio
- 2 different encoding formats supported: Legacy and HQ \(covered later\)

---

#### Existing and new dependencies
You still have to have various things installed on your system\. There's tutorials everywhere, so I'm not going to explain all of it\. These are:
- Python
- Java \(version 8 or above, see note\)
- RGBDS \(Rednex Game Boy Development System\)
- FFMPEG

And if you're running Windows, preferrably:
- WSL \(Windows Subsystem for Linux\)

---

#### Note for Java encoder
I had trouble getting the Java program to work on Windows because of how confusing the command line interface is; thus my addition of the "bash" and "\-c"\. If you're on Linux, you can probably remove it. \(Please, do NOT use the Windows Subsystem for Linux on a Windows machine to do this, it was literally impossible for me to use it to get this working\!\) I also had to compile the Java code in WSL using `javac encoder.java -target 8 -source 8` because I couldn't update to a version beyond Java 8 \(also, ignore any warnings the console gives, it doesn't matter\.\) This shouldn't be a problem on Linux, but if you're on Windows and having issues, try that\.

![Java change 1](/images/Screenshot%20(97).png)
![Java change 2](/images/Screenshot%20(98).png)

---

#### Launching the GUI
On Windows, you can either open Powershell in that directory \(search the internet for how to do it\) and type `python multicoder.py`, or open the latest version of Python IDLE, open the script, and click **Run\->Run Module**\. On Linux, I recommend running it from Python IDLE, because I'm not sure what would happen if you tried to run it from the command line with `python multicoder.py` because of how different the window host system is\. When I tried it with WSL, it just refused to work for whatever reason, so your situation may be different\.

![Launching on Windows](/images/Screenshot%20(99).png)

When the GUI launches, it should look like this:

![Initial GUI screen](/images/Screenshot%20(100).png)

Click on the **Choose audio file\.\.\.** button, and choose the audio file you want to convert\. It should then look something like this:

![After choosing file](/images/Screenshot%20(101).png)

---

#### Using the GUI and description
Here's an overview of the options you can choose from:
- Sample rate\- the box in the middle of the GUI has various sample rates that you can choose from that are calculated based on the other options you choose\. Keep in mind that some higher rates may not work on the original Gameboy due to speed limitations\.
- System\- choose from either Gameboy \(GB\) or Gameboy Color \(GBC\)\. Despite the fact that the Gameboy Advance has Gameboy Color compatibility, the HQ mode will NOT work properly because of how the sound mixing works on the GBA\. Keep in mind that choosing the original GB will restrict the sample rate, as it runs slower than the GBC\.
- Channels\- choose from stereo or mono output\. Mono can reduce the ROM size if you want more audio on a single cartridge without sacrificing sample rate\. Mono input audio files force the output to mono so it doesn't take up unnecessary cartridge space\.
- Encoding mode\- the Legacy mode is a lo\-fi mode that only uses the pulse volume registers for playing back audio, and should work on a Gameboy Advance \(sort of\.\.\. the GBA's pulse channel behavior is wacky, so it doesn't fully work\.\) The HQ mode uses the pulse volume registers in combination with the master volume registers to get a much wider range of amplitudes, which in turn makes the audio higher quality, but takes up more cartridge space, therefore reducing the sample rate\.

When you've picked out all the settings you want, hit the **Make the ROM\!** button, and make sure to choose the directory of all the code, or else it won't work properly\.

![Directory](/images/Screenshot%20(102).png)

While the encoder is working, the window should stop responding\. This is because the Java encoder takes up the CPU processing the window would otherwise use\. When it's done encoding, the window should be back to normal, and depending on the system you chose, there should be either an **audio\.gb** or **audio\.gbc** file\.

### Congrats\! You should now have an audio file that's playable on a real Gameboy\!

---

#### Fixes that might need to be implemented
- Stall the pulses more both on GB and GBC to eliminate occasional noise that isn't significant but kind of strange?
- Tweak the Python size calculation algorithm? \(Output sizes may not be correct for whatever reason\)

---

#### Plans for future patches/updates
- Working on adding some form of compression that doesn't degrade quality or sample rate by a ton
- De\-spaghettifying GUI and encoder code
- Improving UI layout and appearance
- Version 3\.0 \(probably not coming anytime soon\) will probably be written entirely in C++ and will be able to be compiled for any system
- Feel free to mess with the code and make your own version\! I made GBAudioPlayer2 because there wasn't one that was very public already\.

---

### Credits, origin story and technical explanation of how it works
Okay, I'm not the first person to get audio playing on a Gameboy\. Far from it\- **LIJI32's GBVideoPlayer2** for the GBC implements stereo 3-bit PCM with the encoded video, and **ISSOtm's SmoothPlayer** uses a combination of all the Gameboy's channels to produce different DC offsets that are used in combination with the Gameboy's channel mixer to play back stereo 4-bit samples \(and there's probably plenty of others that I don't know, and a lot of tech demos that play back audio\.\) 
I originally got inspiration from GBVideoPlayer2 because I wanted a way to play back audio on a Gameboy without sacrificing cartridge space for video I didn't need\. So, I went on an adventure to figure out how to play audio on the Gameboy\. It took me just a few weeks \(it was in September or October 2020, I forget lol\) to figure out how to use the pulse channels to play back audio, another week to get a player just using the pulse channels working, and surprisingly only another few weeks after conceptualizing the HQ playback method to get THAT working\. I put that code on Github as GBAudioPlayer \(the crappy first version,\) and now after a few MORE months, the method has been perfected\- and now in a neat little package, available to the public and \(hopefully\) easy to use\!
In a way, GBAudioPlayerV2 is a combination of GBVideoPlayer2 and SmoothPlayer\- using some of the Gameboy's standard beep\-boop channels and the 3\-bit master volume register\. However, there is one BIG difference: 
**GBAudioPlayerV2's \(and also V1's\) HQ encoder uses both the pulse volume control and the master volume control to output a much greater range of amplitudes\- 58 total, to be exact\.** 
Basically, it just uses the 3\-bit master volume control as a scaling factor to determine how big the voltage steps of the pulse channels are\. The number of pulse volume steps is reduced from 16 \(the standard 4\-bit range\) to 14 because writing a 0 to the volume registers causes the channel to be disconnected from the output, changing the output to 0 volts instead of the maximum of 1 volt \(the values are basically inverted,\) and having an odd number of steps doesn't really work, but in practice and on paper it doesn't degrade quality at all because it only reduces the amount of unique amplitudes by 2\.
Also, GBAudioPlayer2 is \(I believe\) the *only* audio player for both the original Gameboy and Gameboy Color that uses this method of encoding, as well as be able to output that at *near CD\-quality 43,691Hz stereo* on GBC, and about half that on the original GB\.
