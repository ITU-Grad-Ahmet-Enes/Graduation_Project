import numpy as np
import plotly.graph_objects as go


def fill_arrays(file):
    array = file.read().split('\n')
    array.pop()
    return array


def read_file(run_id):
    file_name = run_id + "outputOnlyNumbersEnergyPower.txt"
    run = open(file_name, "r")
    array = fill_arrays(run)
    return array


run1 = read_file('1')
run2 = read_file('2')
run3 = read_file('3')
run4 = read_file('4')
run5 = read_file('5')
run6 = read_file('6')
run7 = read_file('7')
run8 = read_file('8')
run9 = read_file('9')
run10 = read_file('10')

lambda00 = np.array([])
lambda01 = np.array([])
lambda02 = np.array([])
lambda03 = np.array([])
lambda04 = np.array([])
lambda05 = np.array([])
lambda06 = np.array([])
lambda07 = np.array([])
lambda08 = np.array([])
lambda09 = np.array([])
lambda10 = np.array([])

all_runs = [run1, run2, run3, run4, run5, run6, run7, run8, run9, run10]

Number_of_Brokers = int(run1[0])
Number_of_Tests = int((len(run1) - 1) / (1 + 11 * Number_of_Brokers))

points = [[0 for a in range(11)] for b in range(Number_of_Tests)]
z_points = [0 for c in range(Number_of_Tests)]

current_index = 1
# Calculating Mean
for i in range(0, Number_of_Tests):
    z_points[i] = float(run1[current_index]) * 0.001
    current_index += 1
    lambda_factor = float(0)
    for m in range(0, 11):
        sum_mean = 0
        for k in range(0, Number_of_Brokers):
            for row in range(0, len(all_runs)):
                sum_mean += float(all_runs[row][current_index])
            current_index += 1

        sum_mean /= Number_of_Brokers * len(all_runs)
        sum_with_wrapping = float("{:.2f}".format(sum_mean))

        points[i][m] = [sum_with_wrapping, lambda_factor, z_points[i]]
        lambda_factor += 0.1
        lambda_factor = float("{:.1f}".format(lambda_factor))


current_index = 1
for i in range(0, Number_of_Tests):
    for m in range(0, 11):
        current_index += 1
        for row in range(0, len(all_runs)):
            sum_local_for_std = 0
            for k in range(0, Number_of_Brokers):
                sum_local_for_std += float(all_runs[row][current_index + k])
            sum_local_for_std /= Number_of_Brokers
            if m == 0:
                lambda00 = np.append(lambda00, [sum_local_for_std])
            elif m == 1:
                lambda01 = np.append(lambda01, [sum_local_for_std])
            elif m == 2:
                lambda02 = np.append(lambda02, [sum_local_for_std])
            elif m == 3:
                lambda03 = np.append(lambda03, [sum_local_for_std])
            elif m == 4:
                lambda04 = np.append(lambda04, [sum_local_for_std])
            elif m == 5:
                lambda05 = np.append(lambda05, [sum_local_for_std])
            elif m == 6:
                lambda06 = np.append(lambda06, [sum_local_for_std])
            elif m == 7:
                lambda07 = np.append(lambda07, [sum_local_for_std])
            elif m == 8:
                lambda08 = np.append(lambda08, [sum_local_for_std])
            elif m == 9:
                lambda09 = np.append(lambda09, [sum_local_for_std])
            elif m == 10:
                lambda10 = np.append(lambda10, [sum_local_for_std])
        current_index += 1
    current_index += 1


std = []
start = 0
end = 9
for i in range(0, Number_of_Tests):
    temp = np.array([np.std(lambda00[start:end]), np.std(lambda01[start:end]), np.std(lambda02[start:end]),
                     np.std(lambda03[start:end]), np.std(lambda04[start:end]), np.std(lambda05[start:end]),
                     np.std(lambda06[start:end]), np.std(lambda07[start:end]), np.std(lambda08[start:end]),
                     np.std(lambda09[start:end]), np.std(lambda10[start:end])])
    std.append(temp)
    start += 10
    end += 10


mean_x = [[0 for x in range(11)] for y in range(int(Number_of_Tests))]
mean_y = [[0 for x in range(11)] for y in range(int(Number_of_Tests))]
mean_z = [[0 for x in range(11)] for y in range(int(Number_of_Tests))]

for i in range(0, Number_of_Tests):
    for j in range(0, 11):
        point = points[i][j]
        mean_x[i][j] = point[0]
        mean_y[i][j] = point[1]
        mean_z[i][j] = point[2]

fig = go.Figure()
for i in range(0, Number_of_Tests):
    fig.add_trace(go.Scatter3d(x=mean_y[i], y=mean_x[i], z=mean_z[i],
                               mode='lines+markers',
                               error_y=dict(
                                   type='data',
                                   array=std[i],
                                   visible=True)))
fig.update_layout(
    title_text="X_Lambda, Y_Total Energy Consumption In KWatt, Z_MAX_HAPS_POWER_KWATTS_SEC",
    width=1800,
)
fig.show()

##########################################################################################################

fig = go.Figure()
for i in range(0, Number_of_Tests):
    fig.add_trace(go.Scatter(x=mean_y[i], y=mean_x[i],
                             mode='lines+markers',
                             error_y=dict(
                                 type='data',
                                 array=std[i],
                                 visible=True)))
fig.update_layout(
    title_text="X_Lambda, Y_Total Energy Consumption In KWatt, Z_MAX_HAPS_POWER_KWATTS_SEC",
    width=1800,
)
fig.show()
