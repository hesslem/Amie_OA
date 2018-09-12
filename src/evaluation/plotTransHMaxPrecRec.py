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
    0.013506501556739551,
    0.10929822150403706,
    0.12022443342871482,
    0.35960402635532895,
    0.6113946216832541,
    0.611395643445252,
    0.611395643445252,
    0.6277009214079403,
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
    0.006428026855184732,
    0.0198689966605875,
    0.01325043889381997,
    0.032641475803345,
    0.04966319304093898,
    0.04731312954325204,
    0.04731312954325204,
    0.048472563462654834,
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
    0.059875669821900485,
    0.05986471302164048,
    0.05986471302164048,
    0.05986471302164048



],'rs-', label = 'L1-Norm')
recall, = plt.plot([


    0.0,
    0.0,
    0.0,
    0.01613328135970637,
    0.07570456024300225,
    0.07570456024300225,
    0.17149696136496503,
    0.17149696136496503,
    0.17149696136496503,
    0.17149696136496503,
    0.3821078234783537,
    0.3821078234783537,
    0.6338939911709546,
    0.6338985890999452,
    0.6957981229891511,
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

], [

    0.0,
    0.0,
    0.0,
    0.0,
    0.008167188657075147,
    0.017551670775123613,
    0.010889889398125472,
    0.02086365694059267,
    0.02010432121349795,
    0.017855974847952687,
    0.015267111229352301,
    0.030819981382883378,
    0.030819981382883378,
    0.049105444737111016,
    0.04904682634962029,
    0.053479171912822344,
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
    0.060024522416787,
    0.05986471302164048,
    0.05986471302164048,
    0.05986471302164048,
    0.05986471302164048,
    0.05986471302164048,
    0.05986471302164048,
    0.05822187214272822,
    0.058150416195225464,
    0.05814918048042703






],'b^-', label = 'L2-Norm')




plt.ylabel('Precision')
plt.xlabel('Recall')
plt.xlim(xmin = 0,xmax = 1)
plt.ylim(ymin = 0, ymax = 0.1)
plt.legend()
plt.grid(True)


plt.savefig('plotTransHMaxPrecRec.pdf')
