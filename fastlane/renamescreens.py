#!/usr/bin/python3

import os

for (dirpath, dirnames, filenames) in os.walk("."):
    if dirpath.endswith("/phoneScreenshots"):
        for file in filenames:
            if file.endswith(".png"):
                fileLoc = os.path.join(dirpath, file)
                fileName = os.path.splitext(file)[0]
                try:
                    index = fileName.rindex("_")
                    if index != -1:
                        os.rename(fileLoc, os.path.join(dirpath, fileName[:index] + ".png"))
                except ValueError:
                    pass
