from mpl_toolkits import mplot3d
import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

Number_of_Stations_HAPS = 0
Number_of_Hosts_HAPS = 0
Number_of_Vms_HAPS = 0
Mips_for_Host_HAPS = 0
Ram_for_Host_HAPS = 0
Storage_for_Host_HAPS = 0
BW_for_Host_HAPS = 0
Mips_for_Vm_HAPS = 0
Size_for_Vm_HAPS = 0
Ram_for_Vm_HAPS = 0
BW_for_Vm_HAPS = 0

Number_of_Stations_BASE = 0
Number_of_Hosts_BASE = 0
Number_of_Vms_BASE = 0
Mips_for_Host_BASE = 0
Ram_for_Host_BASE = 0
Storage_for_Host_BASE = 0
BW_for_Host_BASE = 0
Mips_for_Vm_BASE = 0
Size_for_Vm_BASE = 0
Ram_for_Vm_BASE = 0
BW_for_Vm_BASE = 0

outputFile = open("outputOnlyNumbers.txt", "r")

datasetCounter = 0

array = outputFile.read().split('\n')
array.pop()

Number_of_Brokers = int(array[0])
Number_of_Finish_Time = int(Number_of_Brokers) * 11
Number_of_Tests = int((len(array) - 1) / (2 + 11 * Number_of_Brokers))

currentIndex = 1

points = [[0 for x in range(11)] for y in range(Number_of_Tests)]
for i in range(0, Number_of_Tests):
    for j in range(0, 3):
        if j == 0:
            temp = array[currentIndex]
            temp_array = temp.split(',')
            Number_of_Stations_BASE = int(temp_array[0])
            Number_of_Hosts_BASE = int(temp_array[1])
            Number_of_Vms_BASE = int(temp_array[2])
            Mips_for_Host_BASE = int(temp_array[3])
            Ram_for_Host_BASE = int(temp_array[4])
            Storage_for_Host_BASE = int(temp_array[5])
            BW_for_Host_BASE = int(temp_array[6])
            Mips_for_Vm_BASE = int(temp_array[7])
            Size_for_Vm_BASE = int(temp_array[8])
            Ram_for_Vm_BASE = int(temp_array[9])
            BW_for_Vm_BASE = int(temp_array[10])
            currentIndex += 1
        elif j == 1:
            temp = array[currentIndex]
            temp_array = temp.split(',')
            Number_of_Stations_HAPS = int(temp_array[0])
            Number_of_Hosts_HAPS = int(temp_array[1])
            Number_of_Vms_HAPS = int(temp_array[2])
            Mips_for_Host_HAPS = int(temp_array[3])
            Ram_for_Host_HAPS = int(temp_array[4])
            Storage_for_Host_HAPS = int(temp_array[5])
            BW_for_Host_HAPS = int(temp_array[6])
            Mips_for_Vm_HAPS = int(temp_array[7])
            Size_for_Vm_HAPS = int(temp_array[8])
            Ram_for_Vm_HAPS = int(temp_array[9])
            BW_for_Vm_HAPS = int(temp_array[10])
            currentIndex += 1
            #print(temp)
        else:
            lambdaFactor = float(0)
            for m in range(0, 11):
                sum = 0
                for k in range(0, Number_of_Brokers):
                    sum += float(array[currentIndex])
                    currentIndex += 1
                sum /= Number_of_Brokers
                sum_with_wrapping = float("{:.2f}".format(sum))
                power_factor = float(BW_for_Vm_HAPS) / float(BW_for_Vm_BASE)
                if i < (Number_of_Tests/2):
                    points[i][m] = [sum_with_wrapping, lambdaFactor, Number_of_Stations_BASE]
                else:
                    points[i][m] = [sum_with_wrapping, lambdaFactor, power_factor]
                lambdaFactor += 0.1
                lambdaFactor = sum_with_wrapping = float("{:.1f}".format(lambdaFactor))

xdataFirstTest = [[0 for x in range(11)] for y in range(int(Number_of_Tests/2))]
ydataFirstTest = [[0 for x in range(11)] for y in range(int(Number_of_Tests/2))]
zdataFirstTest = [[0 for x in range(11)] for y in range(int(Number_of_Tests/2))]

xdataSecondTest = [[0 for x in range(11)] for y in range(int(Number_of_Tests/2))]
ydataSecondTest = [[0 for x in range(11)] for y in range(int(Number_of_Tests/2))]
zdataSecondTest = [[0 for x in range(11)] for y in range(int(Number_of_Tests/2))]

for i in range(0, Number_of_Tests):
    for j in range(0, 11):
        point = points[i][j]
        if i < Number_of_Tests/2:
            xdataFirstTest[i][j] = point[0]
            ydataFirstTest[i][j] = point[1]
            zdataFirstTest[i][j] = point[2]
        else:
            xdataSecondTest[i-10][j] = point[0]
            ydataSecondTest[i-10][j] = point[1]
            zdataSecondTest[i-10][j] = point[2]

fig = plt.figure(figsize=plt.figaspect(1))

ax1 = fig.add_subplot(1, 2, 1, projection='3d')
for i in range(0,int(Number_of_Tests/2)):
    ax1 = plt.gca(projection="3d")
    ax1.scatter(ydataFirstTest[i], xdataFirstTest[i], zdataFirstTest[i], c='r', marker='o', s=100)
    ax1.set_xlabel('X Label')
    ax1.set_ylabel('Y Label')
    ax1.set_zlabel('Z Label')
    ax1.plot(ydataFirstTest[i], xdataFirstTest[i], zdataFirstTest[i], color='r')

ax2 = fig.add_subplot(1, 2, 2, projection='3d')
for i in range(0,int(Number_of_Tests/2)):
    ax2 = plt.gca(projection="3d")
    ax2.scatter(ydataSecondTest[i], xdataSecondTest[i], zdataSecondTest[i], c='r', marker='o', s=100)
    ax2.set_xlabel('X Label')
    ax2.set_ylabel('Y Label')
    ax2.set_zlabel('Z Label')
    ax2.plot(ydataSecondTest[i], xdataSecondTest[i], zdataSecondTest[i], color='r')

plt.show()


