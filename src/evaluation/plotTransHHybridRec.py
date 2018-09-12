import matplotlib.pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages
import os
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

    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.7895633483072554,
    0.7363298888033447,
    0.7158258502039011,
    0.6992203445074928,
    0.6938249302775157,
    0.6881333753618102





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

    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.785989054544891,
    0.7285840813908361,
    0.7028140517478175,
    0.6958098732521271,
    0.6848734437074703,
    0.6733275331310585,
    0.6422034706189783




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
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.7830203250599561,
    0.7171527784518569,
    0.6967315025742442,
    0.6841570182532672,
    0.6584862698177125,
    0.6361004862054467,
    0.5968707857639263,
    0.5760060651792196







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

    0.8220065055586407,
    0.8220065055586407,
    0.7743716206286254,
    0.6997903174086617,
    0.6821868908276255,
    0.6387981081736022,
    0.5921093748536539,
    0.5625225532674331,
    0.5219168800020572,
    0.47642037264000653,
    0.41705174321962996



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

    0.8220065055586407,
    0.7448810149638747,
    0.6719580314665231,
    0.5736074788891199,
    0.471114532878513,
    0.33880078178416334,
    0.2571834551529075,
    0.22478832071542412,
    0.15604519525786634,
    0.14395485583023357,
    0.13382221239060121





], label = '0.9')


plt.xlabel('Parameter '+r'$\lambda$')
plt.ylabel('Recall')
plt.xlim(xmin = 0,xmax = 1)
plt.ylim(ymin = 0, ymax = 1)
plt.grid(True)
plt.legend()

plt.savefig('plotTransHHybridRec.pdf')