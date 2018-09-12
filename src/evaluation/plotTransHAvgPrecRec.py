import matplotlib.pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages
from matplotlib import pylab
from matplotlib.font_manager import FontProperties
import os
plt.figure()

precision, = plt.plot([

    0.0,
    0.0,
    0.0,
    0.0,
    0.0,
    0.002649428860587217,
    0.07692999346583203,
    0.1093048629570235,
    0.29468484517495885,
    0.43365656774137296,
    0.611395643445252,
    0.611395643445252,
    0.611395643445252,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407
],[


    0.0,
    0.0,
    0.0,
    0.0,
    0.0,
    0.0026851039895315807,
    0.016580160306923945,
    0.015506467681883811,
    0.029487321183429147,
    0.03638031805937959,
    0.04744674621797717,
    0.04731312954325204,
    0.04731312954325204,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612



],'rs-', label = 'L1-Norm')
recall, = plt.plot([

    0.0,
    0.0,
    0.0,
    0.0,
    0.002649428860587217,
    0.07570456024300225,
    0.07570456024300225,
    0.1714955990156345,
    0.17149696136496503,
    0.17149696136496503,
    0.17149696136496503,
    0.3821078234783537,
    0.3821078234783537,
    0.5767081859654518,
    0.6338985890999452,
    0.6338985890999452,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407,
    0.8220065055586407
], [





    0.0,
    0.0,
    0.0,
    0.0,
    0.0034326919449118912,
    0.018849593596939513,
    0.011331326122235317,
    0.021919711350352325,
    0.020110051283460374,
    0.018503855706431004,
    0.015344062879048364,
    0.030819981382883378,
    0.030819981382883378,
    0.045050193061081725,
    0.04904682634962029,
    0.04904682634962029,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06168444913424612,
    0.06166999723819767,
    0.05986675927360136,
    0.059864952090305776,
    0.05986471302164048,
    0.05986471302164048,
    0.05986471302164048,
    0.05986471302164048



], 'b^-',label = 'L2-Norm')




plt.ylabel('Precision')
plt.xlabel('Recall')
plt.xlim(xmin = 0,xmax = 1)
plt.ylim(ymin = 0, ymax = 0.1)
plt.grid(True)
plt.legend()

plt.savefig('plotTransHAvgPrecRec.pdf')