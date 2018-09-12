import matplotlib.pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages
import os
plt.figure()

precision, = plt.plot([
    0.0,
    0.1,
    0.2,
    0.3,
    0.4,
    0.5,
    0.6,
    0.7,
    0.8,
    0.9,
    1

],[

0.06168444913424612,
0.06168444913424612,
0.06168444913424612,
0.06168444913424612,
0.05989463370589033,
0.08484097229189884,
0.2280289593883265,
0.2585982573998364,
0.28032722705406893,
0.2917704633788872,
0.29485947145760594





], label = '0.5'

)

plt.plot([
    0.0,
    0.1,
    0.2,
    0.3,
    0.4,
    0.5,
    0.6,
    0.7,
    0.8,
    0.9,
    1

],[

0.06168444913424612,
0.06168444913424612,
0.06168444913424612,
0.06168444913424612,
0.10758093310976001,
0.27741790330650307,
0.26984491537656313,
0.28927103281842387,
0.2955847269411228,
0.300091418741536,
0.3018578512915326





], label = '0.6'
)

plt.plot([
    0.0,
    0.1,
    0.2,
    0.3,
    0.4,
    0.5,
    0.6,
    0.7,
    0.8,
    0.9,
    1

],[
0.06168444913424612,
0.06168444913424612,
0.06168444913424612,
0.12963339652288922,
0.3536337293312038,
0.4243547479078026,
0.39305802080664837,
0.3025227146315536,
0.3078438212022039,
0.3081802698720153,
0.3067200738211361








], label = '0.7')

plt.plot([
    0.0,
    0.1,
    0.2,
    0.3,
    0.4,
    0.5,
    0.6,
    0.7,
    0.8,
    0.9,
    1

],[
0.06168444913424612,
0.06168444913424612,
0.17116259169960363,
0.4131431798795675,
0.4663161105224202,
0.49871157108997577,
0.5353479261900413,
0.5474538521975376,
0.2988887431355246,
0.291110980259012,
0.27624176341555




], label = '0.8')


plt.plot([
    0.0,
    0.1,
    0.2,
    0.3,
    0.4,
    0.5,
    0.6,
    0.7,
    0.8,
    0.9,
    1

],[
0.06168444913424612,
0.2806736408684424,
0.48868788509863376,
0.5406360805598188,
0.564684863715599,
0.5998705889511228,
0.6134834584944275,
0.6396136238009605,
0.6342728614414281,
0.14201973441579366,
0.13330180450520718




], label = '0.9')


plt.xlabel('Parameter '+r'$\lambda$')
plt.ylabel('Precision')
plt.xlim(xmin = 0,xmax = 1)
plt.ylim(ymin = 0, ymax = 1)
plt.grid(True)
plt.legend()



plt.savefig('plotTransHHybridPrecL2.pdf')







