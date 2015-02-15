#!/bin/sh

pic2plot -Tps $1.pic | ps2pdf -dPDFSETTINGS=/prepress -dEPSCrop - $1.pdf