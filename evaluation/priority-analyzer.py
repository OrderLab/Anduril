# -*- coding: utf-8 -*-
"""
Created on Tue Mar 14 21:04:30 2023

@author: panji
"""
import re
from matplotlib import pyplot as plt

if __name__ == "__main__":
    # Need to be manually changed
    target_id = 213014
    target_occurrence = 77
    ## border line
    
    
    file_index = 0
    ranks = []
    while True:
    
        target_priority = -1
        try:
            file = open("example/"+str(file_index)+".out", "r")
        except:
            print("Finish processing "+str(file_index)+" files!")
            plt.plot(range(len(ranks)),ranks)
            break
            
        lines = file.readlines()
        file.close()
    
        state = 0
        priority_list = [];
    
        for line in lines:
            match_start = re.match('^Flaky Agent Table Start Time:*', line)
            match_stop = re.match('^Using time feedback mode:*', line)
        
        
            if match_start is not None:
                state = 1
                continue 
        
            if state == 0:
                continue
        
            if match_stop is not None:
                break
        
            # Parse out the data here 
            result = re.split(",", line)
            injection_id = int(result[0])
            occurrence = int(result[1])
        
            min_times = 1000000
            for log_priorities in range(2, len(result)-1):
                log_loc_time = re.split("_",result[log_priorities][1:(len(result[log_priorities])-1)])
                temp = int(log_loc_time[1])*int(log_loc_time[2])
                if ( temp < min_times):
                    min_times = temp
               
                if injection_id == target_id and occurrence == target_occurrence: 
                    target_priority = min_times
            
            priority_list.append(min_times)
       
        if target_priority == -1:   
            print("Fail to find the target injection point")
        
        priority_list.sort()    
        rank = 0
        for priority in priority_list:
            if target_priority == priority:
                ranks.append(rank)
                break
            rank = rank + 1
            
        file_index = file_index + 1    
    
        
    
       
        
        
    