import matplotlib.pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages
from matplotlib import pylab
from matplotlib.font_manager import FontProperties
import os
import math
import numpy as np


def sigmoid(x):
    a = []
    for item in x:
        a.append(1-(1/(1+math.exp(-(8*item-5.5)))))
    return a

plt.figure()
x = np.arange(0, 1.5, 0.01)
sig = sigmoid(x)
plt.plot(x,sig,'g')







plt.ylabel('sig(x)')
plt.xlabel('x')

#plt.xlim(xmin = 0,xmax = 1)
#plt.ylim(ymin = 0, ymax = 1)
#plt.legend()
#plt.grid(True)


plt.savefig('sigmoid1.pdf')

