#!/usr/bin/python
from pylab import *
from matplotlib.pyplot import *
from matplotlib.backends.backend_pdf import PdfPages
import numpy as np
import scipy.stats

a=np.genfromtxt('/home/bjoern/pcl/arduino/sketch_160516a/log_20160517_11369_filtered.csv',delimiter=',',dtype='i8',names=None,skip_header=1)

valid=a[a[:,2]>0]
distances = unique(valid[:,2])
data = []
for i in range(len(distances)):
    d = valid[valid[:,2]==distances[i]]
    data.append( d[:,1])

fig,ax = subplots()
ax.boxplot(data)
setp(ax,xticklabels=distances)
