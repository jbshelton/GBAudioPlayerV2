#Gameboy Audio Encoder, Version 2.0, GUI build 1.1, 2/6/2021
#Made by Jackson Shelton
#
#Disclaimer: this is probably some of the most spaghetti Python
#code ever, I literally built this entire thing in 2 days (which
#were also spent learning how to code in Python in the first place!)

import tkinter as tk
from tkinter import *
from tkinter import filedialog
import os
import subprocess

global execbtn
global aud_channels
global sysbtn0
global sysbtn1
global chanbtn0
global chanbtn1
global len_in_seconds
global compat_rates
global l_of_divs
global sratelabel

l_of_divs = []
compat_rates = []

def fchooser():
	global len_in_seconds
	global aud_channels
	tfname = filedialog.askopenfilename()
	fname.set(tfname)
	execbtn.config(state="normal")
	outcmd = ['ffprobe', '-v', 'error', '-show_entries', 'format=duration', '-of', 'default=noprint_wrappers=1:nokey=1']
	outcmd.append(tfname)
	len_in_seconds = subprocess.check_output(outcmd)
	len_in_seconds = round(float(str(len_in_seconds, 'utf-8')))
	outcmd = ['ffprobe', '-v', 'error', '-show_streams', '-of', 'default=noprint_wrappers=1:nokey=1']
	outcmd.append(tfname)
	prc = subprocess.check_output(outcmd)
	prc = str(prc, 'utf-8')
	if prc.find('stereo') != -1:
		aud_channels = 2
		chanbtn1.config(state="normal")
	else:
		aud_channels = 1
		chanbtn1.deselect()
		chanbtn0.select()
		chanbtn1.config(state="disabled")
	calcrate()

def syschooser():
	if sysoption.get() == 0:
		modebtn1.deselect()
		modebtn0.select()
		modebtn1.config(state="disabled")
	else:
		modebtn1.config(state="normal")
	calcrate()
		

def makerom():
	if len(sratelabel.curselection()) == 0:
		execbtn.flash()
	else:
		outrate = sratelabel.get(sratelabel.curselection())
		defaultpath = os.getcwd()
		execbtn.config(text="Generating raw PCM...", state="disabled")
		outpath = filedialog.askdirectory() #pick the directory the python script, Java program, and assembly sources are in!
		encoderpath = outpath
		outpath += "/audio.raw"
		outcmd = ['ffmpeg', '-y', '-i', fname.get(), '-f', 'u8', '-acodec', 'pcm_u8', '-ac', str(aud_channels), '-ar', str(outrate), outpath]
		op = subprocess.run(outcmd)
		print(op)
		execbtn.config(text="Making the ROM...")
		os.chdir(encoderpath)
		outcmd = ['java', 'encoder', str(sysoption.get()), str(modeoption.get()), str(aud_channels), str(l_of_divs[sratelabel.curselection()[0]])]
		op = subprocess.Popen(outcmd, shell=True)
		stdout,stderr = op.communicate()
		print(stdout)
		print(stderr) #these prints are here to debug in case something goes wrong
		execbtn.config(text="Make the ROM!", state="normal")
		os.chdir(defaultpath)
		#system: (0=GB, 1=GBC); mode: (0=legacy, 1=HQ); channel mode: (1=mono, 2=stereo); divider

def calcrate():
	global len_in_seconds
	calsize = 0
	l_of_divs.clear()
	compat_rates.clear()
	sratelabel.delete(0, END)
	for x in range(3, 17):
		if (chanoption.get() == 1 and modeoption.get() == 1) or (chanoption.get() == 2 and modeoption.get() == 0):
			calsize = ((131072/x) * len_in_seconds)
		if chanoption.get() == 1 and modeoption.get() == 0:
			calsize = (((131072/x)/2) * len_in_seconds)
		if chanoption.get() == 2 and modeoption.get() == 1:
			calsize = ((131072/x) * len_in_seconds * 2)
		if calsize > (8388608-16384):
			pass
		else:
			compat_rates.append(round(131072/x))
			if sysoption.get() == 0:
				l_of_divs.append(256-(x*2))
			if sysoption.get() == 1:
				l_of_divs.append(256-x)
	for rate in compat_rates:
		sratelabel.insert(END, rate)
	
window = tk.Tk()
fname = StringVar(window)
sysoption = IntVar(window)
chanoption = IntVar(window)
modeoption = IntVar(window)
mastersrate = StringVar(window)
mastersrate.set('Sample rate appears here')
#note: I never used this but too lazy to rip it out
sratedisp = Frame(window)
sratedisp.grid(row=1)
sratelabel = Listbox(sratedisp, selectmode="single")
sratelabel.pack()
fcframe = Frame(window)
fcframe.grid(row=0)
cfgframe = Frame(window)
cfgframe.grid(row=2)
fcl = Label(fcframe, relief="sunken", textvariable=fname)
fcb = Button(fcframe, text="Choose audio file...", command=fchooser)
fcb.grid(row=0, column=1)
fcl.grid(row=0, column=0)
sysbtn0 = Radiobutton(cfgframe, text="GB", variable=sysoption, value=0, command=syschooser)
sysbtn1 = Radiobutton(cfgframe, text="GBC", variable=sysoption, value=1, command=syschooser)
sysbtn0.grid(row=0, column=0)
sysbtn1.grid(row=1, column=0)
sysbtn1.deselect()
chanbtn0 = Radiobutton(cfgframe, text="Mono", variable=chanoption, value=1, command=calcrate)
chanbtn1 = Radiobutton(cfgframe, text="Stereo", variable=chanoption, value=2, command=calcrate)
chanbtn0.grid(row=0, column=1)
chanbtn1.grid(row=1, column=1)
chanbtn0.deselect()
modebtn0 = Radiobutton(cfgframe, text="Legacy", variable=modeoption, value=0, command=calcrate)
modebtn1 = Radiobutton(cfgframe, text="HQ", variable=modeoption, value=1, command=calcrate)
modebtn0.grid(row=0, column=2)
modebtn1.grid(row=1, column=2)
execframe = Frame(window)
execframe.grid(row=3)
execbtn = Button(execframe, text="Make the ROM!", command=makerom, state="disabled")
execbtn.pack()
window.mainloop()



