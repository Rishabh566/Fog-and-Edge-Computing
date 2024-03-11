2 

Fog and Edge Computing 

Rishabh Sinha             X21171203@student.ncirl.ie MSCLOUD\_23\_B 

National College of Ireland CA Part- C 

**Evaluation Result:** 

For the given scenario for smart home security system, I have done simulation of smart home security system that incorporates smart cameras strategically positioned within a house. The Primary Goal of the project is to evaluate the performance of proposed fog-based architecture in terms of network usage and latency. Simulation is done first including Fog node and then by excluding Fog Node and adding Cloud Node. Simulation is done by gradually increasing the number of cameras for accessing the impact of scale on system performance. 

**Results:** 

The simulation results for the smart home security system includes the performance metrics evaluated and have recorded fog latency, cloud latency, fog network usage, and cloud network usage for the simulation evaluated on iFogSim. The system's performance is assessed by gradually increasing the number of cameras while using the proposed fog-based architecture and a cloud-based alternative. 

*Table 1: Results Table* 



|**Cameras** |**Fog Latency (ms)** |**Cloud Latency (ms)** |**Fog Network Usage (kB)** |**Cloud Network Usage (kB)** |
| - | :- | :- | :- | :- |
|**16** |286\.44 |811\.57 |24912\.2 |429832\.6 |
|**20** |505\.82 |865\.7 |31136\.2 |436056\.6 |
|**24** |580\.13 |901\.78 |37360\.2 |442280\.6 |
|**28** |697\.32 |927\.56 |43584\.2 |448504\.6 |
|**32** |746\.66 |946\.89 |49808\.2 |454728\.6 |
|**40** |792\.14 |973\.947 |62256\.5 |467176\.6 |
|**44** |814\.62 |983\.785 |87168\.7 |473400\.6 |
|**48** |823\.76 |997\.99 |112080\.9 |479624\.6 |

![](Aspose.Words.1d34299f-8236-4b33-95f9-8cb903e5830b.001.png)![](Aspose.Words.1d34299f-8236-4b33-95f9-8cb903e5830b.002.png)

`                `*Fig.1: Cloud Network Usage (kB)                               Fig.2: Fog Network Usage (kB)* 

![](Aspose.Words.1d34299f-8236-4b33-95f9-8cb903e5830b.003.png)![](Aspose.Words.1d34299f-8236-4b33-95f9-8cb903e5830b.004.png)

*Fig.3: Cloud Latency                                                                     Fig.4: Fog Latency*

![](Aspose.Words.1d34299f-8236-4b33-95f9-8cb903e5830b.005.png) ![](Aspose.Words.1d34299f-8236-4b33-95f9-8cb903e5830b.006.png)

`                       `*Fig.5: Fog Vs Cloud Latency                                        Fig.6: Fog Vs Cloud Network usage*                              
