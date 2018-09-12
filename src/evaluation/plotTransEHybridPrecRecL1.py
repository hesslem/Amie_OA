import matplotlib.pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages
import os
plt.figure()








precision, = plt.plot([

    0.8220065055586407,
    0.8179666289125609,
    0.49678596248873297,
    0.14299235602819996,
    0.0788800262388481,
    0.04570017858696787,
    0.006232407599933858,
    0.0,
    0.0,
    0.0

],[0.05814918048042703,
   0.06002543170165472,
   0.07841066229353272,
   0.01681250750718372,
   0.011357566322658112,
   0.008088234850891432,
   0.0052637263840293845,
   0.0,
   0.0,
   0.0



], 's-', markevery = None ,   label = r'$\lambda$'+'=0.2'

)

plt.plot([


    0.8220065055586407,
    0.8219412830844406,
    0.803089093048289,
    0.7164998725351908,
    0.11818005796455815,
    0.00852507122958328,
    4.657531773817996E-4,
    3.2747472033096915E-4,
    0.0,
    0.0


],[0.05814918048042703,
   0.060621572798039675,
   0.06510188341971973,
   0.07802066029145655,
   0.021286864903966356,
   0.05644943951990745,
   0.03187868615520899,
   0.05527609301790796,
   0.0,
   0.0



],'^-',markevery = None, label = r'$\lambda$'+'=0.4'
)

plt.plot([


    0.8220065055586407,
    0.8219356633934521,
    0.8013045857189347,
    0.7150067376689079,
    0.6493007997501451,
    0.42301661946977703,
    0.016219109367531025,
    5.776361161532228E-4,
    0.0,
    0.0



],[
    0.05814918048042703,
    0.06077265736209277,
    0.07841066229353272,
    0.15782090151744413,
    0.2917504475925345,
    0.2733324471819285,
    0.023419384568021753,
    0.08070425886271711,
    0.0,
    0.0





],'o-', label = r'$\lambda$'+'=0.6')

plt.plot([

    0.8220065055586407,
    0.8115395756520246,
    0.7392177287967782,
    0.7083296933062158,
    0.6903759726535619,
    0.6481089143795802,
    0.5580438298432497,
    0.26257648527155625,
    0.003987596490520007,
    0.0



],[0.05814918048042703,
   0.06996509915293653,
   0.19885022703700797,
   0.25902406614838935,
   0.2893271868562851,
   0.30120412606245256,
   0.30497698956780367,
   0.2150222621861902,
   0.007185808825447387,
   0.0



], '-x',label = r'$\lambda$'+'=0.8')





plt.ylabel('Precision')
plt.xlabel('Recall')
plt.xlim(xmin = 0,xmax = 1)
plt.ylim(ymin = 0, ymax = 1)
plt.grid(True)
plt.legend()



plt.savefig('plotTransEHybridprerecL1.pdf')











