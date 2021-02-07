# GBAudioPlayerV2
Version 2\.0 of the incredible Gameboy Audio Player, now with a GUI and some more features\!

#### What this new version is
I made significant improvements to both the source assembly code and the Java encoder program, as well as shoving in a brand\-new easy\-to\-use graphical interface for all of it\!

---

#### Improvements/fixes to past issues
- Not \(completely\) dependent on the command line anymore
- You don't have to use Audacity or a hex editor to put the audio in the ROM file
- Audio encodes directly into the ROM file to minimize encoding time

---

#### New features
- One single interface to rule them all\!
- Selection of various sample rates
- Configuration for both the original Gameboy and Gameboy Color
- Select from stereo/mono, and auto mono configuration for input audio
- 2 different encoding formats supported for GBC: Legacy and HQ \(covered later\)

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
I had trouble getting the Java program to work on Windows because of how confusing the command line interface is; thus my addition of the "bash" and "\-c"\. If you're on Linux, you can probably remove it. \(Please, do NOT use the Windows Subsystem for Linux on a Windows machine to do this, it was literally impossible for me to use it to get this working\!\) I also had to compile the Java code in WSL using `javac encoder.java -target 8 -source 8` because I couldn't update to a version beyond Java 8\. This shouldn't be a problem on Linux, but if you're on Windows and having issues, try that\.

![Java change 1](/images/Screenshot (97).png)
![Java change 2](/images/Screenshot (98).png)

---

#### Launching the GUI
On Windows, you can either open Powershell and type `python multicoder.py`, or open the latest version of Python IDLE, open the script, and click **Run\->Run Module**\. On Linux, I recommend running it from Python IDLE, because I'm not sure what would happen if you tried to run it from the command line with `python multicoder.py` because of how different the window host system is\. When I tried it with WSL, it just refused to work for whatever reason, so your situation may be different\.

![Launching on Windows](/images/Screenshot (99).png)

When the GUI launches, it should look like this:

![Initial GUI screen](/images/Screenshot (100).png)

Click on the **Choose audio file\.\.\.** button, and choose the audio file you want to convert\. It should then look something like this:

![After choosing file](/images/Screenshot (101).png)

---

#### Using the GUI and description
Here's an overview of the options you can choose from:
- Sample rate\- the box in the middle of the GUI has various sample rates that you can choose from that are calculated based on the other options you choose\. Keep in mind that some higher rates may not work on the original Gameboy due to speed limitations\.
- System\- choose from either Gameboy \(GB\) or Gameboy Color \(GBC\)\. Despite the fact that the Gameboy Advance has Gameboy Color compatibility, the HQ mode special to GBC will NOT work properly because of how the sound mixing works on the GBA\. HQ mode is not compatible with the original Gameboy for the same reason\.
- Channels\- choose from stereo or mono output\. Mono can reduce the ROM size if you want more audio on a single cartridge without sacrificing sample rate\. Mono input audio files force the output to mono so it doesn't take up unnecessary cartridge space\.
- Encoding mode\- the Legacy mode is a lo\-fi mode that only uses the pulse volume registers for playing back audio\. The HQ mode, which only works on the Gameboy Color, uses the pulse volume registers in combination with the master volume registers to get a much wider range of amplitudes, which in turn makes the audio higher quality, but takes up more cartridge space, therefore reducing the sample rate\.

When you've picked out all the settings you want, hit the **Make the ROM\!** button, and make sure to choose the directory of all the code, or else it won't work properly\.

![Directory](/images/Screenshot (102).png)

While the encoder is working, the window should stop responding\. This is because the Java encoder takes up the CPU processing the window would otherwise use\. When it's done encoding, the window should be back to normal, and depending on the system you chose, there should be either an **audio\.gb** or **audio\.gbc** file\.

### Congrats\! You should now have an audio file that's playable on a real Gameboy\!
I have not fully tested the Legacy mode yet, so it may not work\- please notify me if it's broken\!
