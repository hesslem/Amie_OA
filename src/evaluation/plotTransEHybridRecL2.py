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
0.07364962657153382,
0.07470459583437851,
0.07799279623732738,
0.020230887558668295,
0.012083868268312658,
0.04568127599000649,
0.43905130079668486,
0.5959403011711606,
0.6482310149383307,
0.68154181841961,
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
0.07364962657153382,
0.04570017858696787,
0.009527249455869163,
0.004297530963220144,
0.004735526272991986,
0.005734468919617814,
0.01934706343047395,
0.3215641677603777,
0.5615712928473765,
0.6068423313952211,
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
0.045697964769305724,
0.002974689763256042,
3.974654171879782E-4,
3.2747472033096915E-4,
3.2747472033096915E-4,
4.3322708711491706E-4,
0.0013824439831756669,
0.0076947193126130214,
0.279320610012348,
0.49467874866170464,
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

0.002647215042925073,
0.0,
0.0,
0.0,
0.0,
0.0,
0.0,
1.0575236678394791E-4,
0.004030510494432334,
0.16190874677655376,
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

0.0,
0.0,
0.0,
0.0,
0.0,
0.0,
0.0,
0.0,
0.0,
0.003466838458917249,
0.13382221239060121







], label = '0.9')


plt.xlabel('Parameter '+r'$\lambda$')
plt.ylabel('Recall')
plt.xlim(xmin = 0,xmax = 1)
plt.ylim(ymin = 0, ymax = 1)
plt.grid(True)
plt.legend()


plt.savefig('plotTransEHybridRecL2.pdf')
