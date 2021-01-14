from mpl_toolkits import mplot3d
import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import plotly.graph_objects as go
from plotly.subplots import make_subplots

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

outputFile = open("outputOnlyNumbersTime.txt", "r")
outputFile2 = open("outputOnlyNumbersEnergy.txt", "r")
outputFile3 = open("outputOnlyNumbersEnergyPower.txt", "r")

datasetCounter = 0

array = outputFile.read().split('\n')
array.pop()

array2 = outputFile2.read().split('\n')
array2.pop()

array3 = outputFile3.read().split('\n')
array3.pop()

Number_of_Brokers = int(array[0])
Number_of_Finish_Time = int(Number_of_Brokers) * 11
Number_of_Tests = int((len(array) - 1) / (2 + 11 * Number_of_Brokers))

Number_of_Brokers_Energy = int(array2[0])
Number_of_Lines = int(Number_of_Brokers_Energy) * 11 + 1
Number_of_Tests_Energy = int((len(array2) - 1) / (1 + 11 * Number_of_Brokers_Energy))

Number_of_Brokers_Energy_Power = int(array3[0])
Number_of_Tests_Energy_Power = int((len(array3) - 1) / (1 + 11 * Number_of_Brokers_Energy_Power))

points = [[0 for x in range(11)] for y in range(Number_of_Tests)]

points_Energy = [[0 for m in range(11)] for n in range(Number_of_Tests_Energy)]
z_points_energy = [0 for t in range(Number_of_Tests_Energy)]

points_Energy_Power = [[0 for l in range(11)] for b in range(Number_of_Tests_Energy_Power)]
z_points_energy_power = [0 for c in range(Number_of_Tests_Energy_Power)]

currentIndexEnergy = 1

for i in range(0, Number_of_Tests_Energy):
    z_points_energy[i] = float(array2[currentIndexEnergy]) * 0.001
    currentIndexEnergy += 1
    lambdaFactorEnergy = float(0)
    for m in range(0, 11):
        sum = 0
        for k in range(0, Number_of_Brokers_Energy):
            sum += float(array2[currentIndexEnergy])
            currentIndexEnergy += 1

        sum /= Number_of_Brokers_Energy
        sum_with_wrapping = float("{:.2f}".format(sum))

        points_Energy[i][m] = [sum_with_wrapping, lambdaFactorEnergy, z_points_energy[i]]
        lambdaFactorEnergy += 0.1
        lambdaFactorEnergy = sum_with_wrapping = float("{:.1f}".format(lambdaFactorEnergy))

currentIndexEnergyPower = 1
for i in range(0, Number_of_Tests_Energy_Power):
    z_points_energy_power[i] = float(array3[currentIndexEnergyPower]) * 0.001
    currentIndexEnergyPower += 1
    lambdaFactorEnergyPower = float(0)
    for m in range(0, 11):
        sum = 0
        for k in range(0, Number_of_Brokers_Energy_Power):
            sum += float(array3[currentIndexEnergyPower])
            currentIndexEnergyPower += 1

        sum /= Number_of_Brokers_Energy_Power
        sum_with_wrapping = float("{:.2f}".format(sum))

        points_Energy_Power[i][m] = [sum_with_wrapping, lambdaFactorEnergyPower, z_points_energy_power[i]]
        lambdaFactorEnergyPower += 0.1
        lambdaFactorEnergyPower = sum_with_wrapping = float("{:.1f}".format(lambdaFactorEnergyPower))

currentIndex = 1
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

xdataThirdTest = [[0 for x in range(11)] for y in range(int(Number_of_Tests_Energy))]
ydataThirdTest = [[0 for x in range(11)] for y in range(int(Number_of_Tests_Energy))]
zdataThirdTest = [[0 for x in range(11)] for y in range(int(Number_of_Tests_Energy))]

xdataFourthTest = [[0 for x in range(11)] for y in range(int(Number_of_Tests_Energy_Power))]
ydataFourthTest = [[0 for x in range(11)] for y in range(int(Number_of_Tests_Energy_Power))]
zdataFourthTest = [[0 for x in range(11)] for y in range(int(Number_of_Tests_Energy_Power))]

for i in range(0, Number_of_Tests_Energy):
    for j in range(0, 11):
        point = points_Energy[i][j]
        xdataThirdTest[i][j] = point[0]
        ydataThirdTest[i][j] = point[1]
        zdataThirdTest[i][j] = point[2]

for i in range(0, Number_of_Tests_Energy_Power):
    for j in range(0, 11):
        point = points_Energy_Power[i][j]
        xdataFourthTest[i][j] = point[0]
        ydataFourthTest[i][j] = point[1]
        zdataFourthTest[i][j] = point[2]

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

# fig1 = plt.figure(1)
# ax1 = fig1.add_subplot(111, projection='3d')
# for i in range(0,int(Number_of_Tests/2)):
#     ax1 = plt.gca(projection="3d")
#     ax1.scatter(ydataFirstTest[i], xdataFirstTest[i], zdataFirstTest[i], c='r', marker='o', s=100)
#     ax1.set_xlabel('X Lambda')
#     ax1.set_ylabel('Y Time')
#     ax1.set_zlabel('Z Number of Base')
#     ax1.plot(ydataFirstTest[i], xdataFirstTest[i], zdataFirstTest[i], color='r')
#
# fig2 = plt.figure(2)
# ax2 = fig2.add_subplot(111, projection='3d')
# for i in range(0,int(Number_of_Tests/2)):
#     ax2 = plt.gca(projection="3d")
#     ax2.scatter(ydataSecondTest[i], xdataSecondTest[i], zdataSecondTest[i], c='r', marker='o', s=100)
#     ax2.set_xlabel('X Lambda')
#     ax2.set_ylabel('Y Time')
#     ax2.set_zlabel('Z HAPS/BASE Power')
#     ax2.plot(ydataSecondTest[i], xdataSecondTest[i], zdataSecondTest[i], color='r')
#
# fig3 = plt.figure(3)
# ax3 = fig3.add_subplot(111, projection='3d')
# for i in range(0, int(Number_of_Tests_Energy)):
#     ax3 = plt.gca(projection="3d")
#     ax3.scatter(ydataThirdTest[i], xdataThirdTest[i], zdataThirdTest[i], c='r', marker='o', s=100)
#     ax3.set_xlabel('X Lambda')
#     ax3.set_ylabel('Y Total Energy')
#     ax3.set_zlabel('Z MAX_HAPS_POWER_WATTS_SEC')
#     ax3.plot(ydataThirdTest[i], xdataThirdTest[i], zdataThirdTest[i], color='r')
#
# plt.show()


fig = go.Figure()
for i in range(0, int(Number_of_Tests/2)):
    fig.add_trace(go.Scatter3d(x=ydataFirstTest[i], y=xdataFirstTest[i], z=zdataFirstTest[i],
                               mode='lines+markers'))
fig.update_layout(
    title_text="1: X_Lambda, Y_Time, Z_Number of Base",
    width=1800,
)
fig.show()
#######################
fig = go.Figure()
for i in range(0, int(Number_of_Tests/2)):
    fig.add_trace(go.Scatter(x=ydataFirstTest[i], y=xdataFirstTest[i],
                             mode='lines+markers'))
fig.update_layout(
    title_text="1 2d: X_Lambda, Y_Time, Z_Number of Base",
    width=1800,
)
fig.show()

#######################
fig = go.Figure()
for i in range(0, int(Number_of_Tests/2)):
    fig.add_trace(go.Scatter3d(x=ydataSecondTest[i], y=xdataSecondTest[i], z=zdataSecondTest[i],
                               mode='lines+markers'))
fig.update_layout(
    title_text="2: X_Lambda, Y_Time, Z_HAPS/BASE Power",
    width=1800,
)
fig.show()
#######################
fig = go.Figure()
for i in range(0, int(Number_of_Tests/2)):
    fig.add_trace(go.Scatter(x=ydataSecondTest[i], y=xdataSecondTest[i],
                             mode='lines+markers'))
fig.update_layout(
    title_text="2 2d: X_Lambda, Y_Time, Z_HAPS/BASE Power",
    width=1800,
)
fig.show()
#######################
fig = go.Figure()
for i in range(0, Number_of_Tests_Energy):
    fig.add_trace(go.Scatter3d(x=ydataThirdTest[i], y=xdataThirdTest[i], z=zdataThirdTest[i],
                               mode='lines+markers'))
fig.update_layout(
    title_text="3: X_Lambda, Y_Total Energy Consumption In KWatt, Z_MAX_HAPS_POWER_KWATTS_SEC",
    width=1800,
)
fig.show()
#######################
fig = go.Figure()
for i in range(0, int(Number_of_Tests/2)):
    fig.add_trace(go.Scatter(x=ydataThirdTest[i], y=xdataThirdTest[i],
                             mode='lines+markers'))
fig.update_layout(
    title_text="3 2d: X_Lambda, Y_Total Energy Consumption In KWatt, Z_MAX_HAPS_POWER_KWATTS_SEC",
    width=1800,
)
fig.show()
#######################
fig = go.Figure()
for i in range(0, Number_of_Tests_Energy_Power):
    fig.add_trace(go.Scatter3d(x=ydataFourthTest[i], y=xdataFourthTest[i], z=zdataFourthTest[i],
                               mode='lines+markers'))
fig.update_layout(
    title_text="4: X_Lambda, Y_Total Energy Consumption In KWatt, Z_MAX_HAPS_POWER_KWATTS_SEC",
    width=1800,
)
fig.show()
#######################
fig = go.Figure()
for i in range(0, int(Number_of_Tests/2)):
    fig.add_trace(go.Scatter(x=ydataFourthTest[i], y=xdataFourthTest[i],
                             mode='lines+markers'))
fig.update_layout(
    title_text="4 2d: X_Lambda, Y_Total Energy Consumption In KWatt, Z_MAX_HAPS_POWER_KWATTS_SEC",
    width=1800,
)
fig.show()