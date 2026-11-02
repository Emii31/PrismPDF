import os
import time
import shutil
import subprocess
import sys

transforms_dir = os.path.expanduser("~/.gradle/caches/transforms-4")

with open("aapt2_wrapper.py", "r") as f:
    wrapper_code = f.read()

os.environ["ANDROID_HOME"] = "/data/data/com.termux/files/home/android-sdk"

print("Starting Gradle build with native-delegating aapt2 wrapper...")
p = subprocess.Popen(["./gradlew", "clean", "assembleDebug"])

try:
    while p.poll() is None:
        if os.path.exists(transforms_dir):
            for root, dirs, files in os.walk(transforms_dir):
                if "aapt2" in files:
                    target = os.path.join(root, "aapt2")
                    try:
                        with open(target, "r", encoding="utf-8", errors="ignore") as f:
                            content = f.read(50)
                        is_correct_wrapper = "#!/data/data/com.termux/files/usr/bin/python3" in content and "subprocess.run(cmd)" in content
                    except Exception:
                        is_correct_wrapper = False

                    if not is_correct_wrapper:
                        try:
                            with open(target, "w", encoding="utf-8") as f:
                                f.write(wrapper_code)
                            os.chmod(target, 0o755)
                        except Exception as e:
                            pass
        time.sleep(0.2)
except KeyboardInterrupt:
    p.terminate()

exit(p.returncode)
