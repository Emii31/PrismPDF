#!/data/data/com.termux/files/usr/bin/python3
import sys
import os
import subprocess

real_aapt2 = "/data/data/com.termux/files/home/android-sdk/build-tools/34.0.0/aapt2"
args = sys.argv[1:]

fixed_args = []
for arg in args:
    if "android-35" in arg:
        arg = arg.replace("android-35", "android-34")
    fixed_args.append(arg)

cmd = [real_aapt2] + fixed_args
res = subprocess.run(cmd)
sys.exit(res.returncode)
