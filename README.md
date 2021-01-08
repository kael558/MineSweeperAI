TODO
for training data
play through game
at each point, serialize entire observableboard, action & reward into bytes

train look-up for 5x5
byte file storing every combination of bombs in 5x5 as byte index
two ints stored (num times visited, num times should click)
get 5x5 state, get all possible bomb states 
use average to figure out % that middle square is bomb
if % is >0.9, then flag
if % is <0.1, then click

train 5x5
State: bytes[] -> observableboard 
foreach square in observableBoard -> INDArray 5x5
fit(5x5 state, reward for click/flag of one square)
argmax output Q for both actions

train full - q
State: bytes[] -> observableboard -> INDArray full size
fit(state, reward for click/flag 
argmax output Q for all actions

pi(s) takes state, returns action
V(s) takes state, returns value of state
Q(s,a) takes state& action, return value of taking action a in state s
