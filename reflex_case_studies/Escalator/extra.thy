theory extra
imports EscalatorTheory
begin

definition eCtrlStates where
"eCtrlStates s \<equiv>
(\<forall>s1. toEnvP s1 \<and> substate s1 s \<longrightarrow>
  getPstate s1 ''Ctrl'' \<in> {''motionless'',''goUp'',''goDown'',''stuckState'', ''emergency''})"

definition eGoUpOut where
"eGoUpOut s \<equiv> 
(\<forall>s1. toEnvP s1 \<and> substate s1 s \<and>
  getPstate s1 ''Ctrl'' = ''goUp''\<longrightarrow>
    getVarBool s1 ''out_0'' = True \<and>
    getVarBool s1 ''out_1'' = False \<and>
    getVarBool s1 ''direction'' = True \<and>
    getVarBool s1 ''moving'' = True)"

definition eGoDownOut where
"eGoDownOut s \<equiv> 
(\<forall>s1. toEnvP s1 \<and> substate s1 s \<and>
  getPstate s1 ''Ctrl'' = ''goDown''\<longrightarrow>
    getVarBool s1 ''out_0'' = False \<and>
    getVarBool s1 ''out_1'' = True \<and>
    getVarBool s1 ''direction'' = False \<and>
    getVarBool s1 ''moving'' = True)"

definition eMotionlessOut where
"eMotionlessOut s \<equiv> 
(\<forall>s1. toEnvP s1 \<and> substate s1 s \<and>
  getPstate s1 ''Ctrl'' = ''motionless''\<longrightarrow>
    getVarBool s1 ''out_0'' = False \<and>
    getVarBool s1 ''out_1'' = False \<and>
    getVarBool s1 ''moving'' = False)"

definition eStuckStateOut where
"eStuckStateOut s \<equiv> 
(\<forall>s1. toEnvP s1 \<and> substate s1 s \<and>
  getPstate s1 ''Ctrl'' = ''stuckState'' \<and>
  getPstate (predEnv s1) ''Ctrl'' = ''stuckState'' \<longrightarrow>
    getVarBool s1 ''out_0'' = False \<and>
    getVarBool s1 ''out_1'' = False)"

definition eEmergencyOut where
"eEmergencyOut s \<equiv> 
(\<forall>s1. toEnvP s1 \<and> substate s1 s \<and>
  getPstate s1 ''Ctrl'' = ''emergency'' \<and>
  getPstate (predEnv s1) ''Ctrl'' = ''emergency'' \<longrightarrow>
    getVarBool s1 ''out_0'' = False \<and>
    getVarBool s1 ''out_1'' = False)"

definition eMotionlessTrans where
"eMotionlessTrans s \<equiv>
(\<forall>s1. toEnvP s1 \<and> substate s1 s \<and>
  getPstate s1 ''Ctrl'' = ''motionless'' \<longrightarrow>
    (\<forall>s2. toEnvP s2 \<and> substate s2 s1 \<longrightarrow>
      getPstate s2 ''Ctrl'' = ''motionless'') \<or>
    (\<exists>s2 s3. toEnvP s2 \<and> toEnvP s3 \<and>
      substate s2 s3 \<and> substate s3 s1 \<and>
      getPstate s2 ''Ctrl'' = ''goUp'' \<and>
      ltime s2 ''Ctrl'' \<ge> 120000 \<and>
      toEnvNum s2 s3 = 1 \<and>
      getPstate s3 ''Ctrl'' = ''motionless'' \<and>
      (\<forall>s4. toEnvP s4 \<and> substate s3 s4 \<and> substate s4 s1 \<longrightarrow>
        getPstate s4 ''Ctrl'' = ''motionless'')) \<or>
    (\<exists>s2 s3. toEnvP s2 \<and> toEnvP s3 \<and>
      substate s2 s3 \<and> substate s3 s1 \<and>
      getPstate s2 ''Ctrl'' = ''goDown'' \<and>
      ltime s2 ''Ctrl'' \<ge> 120000 \<and>
      toEnvNum s2 s3 = 1 \<and>
      getPstate s3 ''Ctrl'' = ''motionless'' \<and>
      (\<forall>s4. toEnvP s4 \<and> substate s3 s4 \<and> substate s4 s1 \<longrightarrow>
        getPstate s4 ''Ctrl'' = ''motionless'')) \<or>
    (\<exists>s2 s3. toEnvP s2 \<and> toEnvP s3 \<and>
      substate s2 s3 \<and> substate s3 s1 \<and>
      getPstate s2 ''Ctrl'' = ''stuckState'' \<and>
      ltime s2 ''Ctrl'' \<ge> 1000 \<and>
      getVarBool s2 ''moving'' = False \<and>
      toEnvNum s2 s3 = 1 \<and>
      getPstate s3 ''Ctrl'' = ''motionless'' \<and>
      (\<forall>s4. toEnvP s4 \<and> substate s3 s4 \<and> substate s4 s1 \<longrightarrow>
        getPstate s4 ''Ctrl'' = ''motionless'')))"

definition eGoUpTrans where
"eGoUpTrans s \<equiv>
(\<forall>s1. toEnvP s1 \<and> substate s1 s \<and>
  getPstate s1 ''Ctrl'' = ''goUp'' \<longrightarrow>
    (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and>
      substate s2 s3 \<and> substate s3 s1 \<and>
      toEnvNum s2 s3 = 1 \<and>
      getPstate s2 ''Ctrl'' = ''motionless'' \<and>
      getVarBool s2 ''inp_3'' = False \<and>
      getVarBool s2 ''inp_4'' = False \<and>
      (getVarBool s2 ''inp_0'' \<or> getVarBool s2 ''inp_1'')= True \<and>
      getVarBool s2 ''inp_2'' = True \<and>
      getPstate s3 ''Ctrl'' = ''goUp'' \<and>
      (\<forall>s4. toEnvP s4 \<and> substate s3 s4 \<and> substate s4 s1 \<longrightarrow>
        getPstate s4 ''Ctrl'' = ''goUp'')) \<or>
    (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and>
      substate s2 s3 \<and> substate s3 s1 \<and>
      toEnvNum s2 s3 = 1 \<and>
      getPstate s2 ''Ctrl'' = ''stuckState'' \<and>
      getVarBool s2 ''inp_3'' = False \<and>
      getVarBool s2 ''inp_4'' = False \<and>
      ltime s2 ''Ctrl'' \<ge> 1000 \<and>
      getVarBool s2 ''moving'' = True \<and>
      getVarBool s2 ''direction'' = True \<and>
      getPstate s3 ''Ctrl'' = ''goUp'' \<and>
      (\<forall>s4. toEnvP s4 \<and> substate s3 s4 \<and> substate s4 s1 \<longrightarrow>
        getPstate s4 ''Ctrl'' = ''goUp'')))"

definition eGoDownTrans where
"eGoDownTrans s \<equiv>
(\<forall>s1. toEnvP s1 \<and> substate s1 s \<and>
  getPstate s1 ''Ctrl'' = ''goDown'' \<longrightarrow>
    (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and>
      substate s2 s3 \<and> substate s3 s1 \<and>
      toEnvNum s2 s3 = 1 \<and>
      getPstate s2 ''Ctrl'' = ''motionless'' \<and>
      getVarBool s2 ''inp_3'' = False \<and>
      getVarBool s2 ''inp_4'' = False \<and>
      (getVarBool s2 ''inp_0'' \<or> getVarBool s2 ''inp_1'')= True \<and>
      getVarBool s2 ''inp_2'' = False \<and>
      getPstate s3 ''Ctrl'' = ''goDown'' \<and>
      (\<forall>s4. toEnvP s4 \<and> substate s3 s4 \<and> substate s4 s1 \<longrightarrow>
        getPstate s4 ''Ctrl'' = ''goDown'')) \<or>
    (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and>
      substate s2 s3 \<and> substate s3 s1 \<and>
      toEnvNum s2 s3 = 1 \<and>
      getPstate s2 ''Ctrl'' = ''stuckState'' \<and>
      getVarBool s2 ''inp_3'' = False \<and>
      getVarBool s2 ''inp_4'' = False \<and>
      ltime s2 ''Ctrl'' \<ge> 1000 \<and>
      getVarBool s2 ''moving'' = True \<and>
      getVarBool s2 ''direction'' = False \<and>
      getPstate s3 ''Ctrl'' = ''goDown'' \<and>
      (\<forall>s4. toEnvP s4 \<and> substate s3 s4 \<and> substate s4 s1 \<longrightarrow>
        getPstate s4 ''Ctrl'' = ''goDown'')))"

definition eStuckStateTrans where
"eStuckStateTrans s \<equiv>
(\<forall>s1. toEnvP s1 \<and> substate s1 s \<and>
  getPstate s1 ''Ctrl'' = ''stuckState'' \<longrightarrow>
    (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and>
      substate s2 s3 \<and> substate s3 s1 \<and>
      toEnvNum s2 s3 = 1 \<and>
      getPstate s2 ''Ctrl'' = ''motionless'' \<and>
      getVarBool s2 ''inp_3'' = False \<and>
      getVarBool s2 ''inp_4'' = True \<and>
      getPstate s3 ''Ctrl'' = ''stuckState'' \<and>
      (\<forall>s4. toEnvP s4 \<and> substate s3 s4 \<and> substate s4 s1 \<longrightarrow>
        getPstate s4 ''Ctrl'' = ''stuckState'')) \<or>
    (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and>
      substate s2 s3 \<and> substate s3 s1 \<and>
      toEnvNum s2 s3 = 1 \<and>
      getPstate s2 ''Ctrl'' = ''goUp'' \<and>
      getVarBool s2 ''inp_3'' = False \<and>
      getVarBool s2 ''inp_4'' = True \<and>
      getPstate s3 ''Ctrl'' = ''stuckState'' \<and>
      (\<forall>s4. toEnvP s4 \<and> substate s3 s4 \<and> substate s4 s1 \<longrightarrow>
        getPstate s4 ''Ctrl'' = ''stuckState'')) \<or>
    (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and>
      substate s2 s3 \<and> substate s3 s1 \<and>
      toEnvNum s2 s3 = 1 \<and>
      getPstate s2 ''Ctrl'' = ''goSown'' \<and>
      getVarBool s2 ''inp_3'' = False \<and>
      getVarBool s2 ''inp_4'' = True \<and>
      getPstate s3 ''Ctrl'' = ''stuckState'' \<and>
      (\<forall>s4. toEnvP s4 \<and> substate s3 s4 \<and> substate s4 s1 \<longrightarrow>
        getPstate s4 ''Ctrl'' = ''stuckState'')))"

definition eEmergencyTrans where
"eEmergencyTrans s \<equiv>
(\<forall>s1. toEnvP s1 \<and> substate s1 s \<and>
  getPstate s1 ''Ctrl'' = ''emergency'' \<longrightarrow>
    (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and>
      substate s2 s3 \<and> substate s3 s1 \<and>
      toEnvNum s2 s3 = 1 \<and>
      getPstate s2 ''Ctrl'' = ''motionless'' \<and>
      getVarBool s2 ''inp_3'' = True \<and>
      getPstate s3 ''Ctrl'' = ''emergency'') \<or>
    (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and>
      substate s2 s3 \<and> substate s3 s1 \<and>
      toEnvNum s2 s3 = 1 \<and>
      getPstate s2 ''Ctrl'' = ''goUp'' \<and>
      getVarBool s2 ''inp_3'' = True \<and>
      getPstate s3 ''Ctrl'' = ''emergency'') \<or>
    (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and>
      substate s2 s3 \<and> substate s3 s1 \<and>
      toEnvNum s2 s3 = 1 \<and>
      getPstate s2 ''Ctrl'' = ''goDown'' \<and>
      getVarBool s2 ''inp_3'' = True \<and>
      getPstate s3 ''Ctrl'' = ''emergency'') \<or>
    (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and>
      substate s2 s3 \<and> substate s3 s1 \<and>
      toEnvNum s2 s3 = 1 \<and>
      getPstate s2 ''Ctrl'' = ''stuckState'' \<and>
      getVarBool s2 ''inp_3'' = True \<and>
      getPstate s3 ''Ctrl'' = ''emergency''))"

definition eEmergencyLast where
"eEmergencyLast s \<equiv>
(\<forall>s1. toEnvP s1 \<and> substate s1 s \<and>
  getPstate s1 ''Ctrl'' = ''emergency'' \<longrightarrow>
  (\<forall>s2. toEnvP s2 \<and> substate s1 s2 \<and> substate s2 s \<longrightarrow>
    getPstate s2 ''Ctrl'' = ''emergency''))"

definition extraInv:
"extraInv s \<equiv> toEnvP s \<and>
eCtrlStates s \<and>
eMotionlessOut s \<and>
eGoUpOut s \<and>
eGoDownOut s \<and>
eStuckStateOut s \<and>
eEmergencyOut s \<and>
eMotionlessTrans s \<and>
eGoUpTrans s \<and>
eGoDownTrans s \<and>
eStuckStateTrans s \<and>
eEmergencyTrans s \<and>
eEmergencyLast s"

definition extraInv2 where " extraInv2 s \<equiv> toEnvP s \<and>
(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> 
getPstate s1 Ctrl' = Ctrl'motionless \<longrightarrow>
  (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and> 
  substate s2 s3 \<and> substate s3 s1 \<and> toEnvNum s2 s3 = 1 \<and>
  ((getPstate s2 Ctrl' = Ctrl'goUp \<or> getPstate s2 Ctrl' = Ctrl'goDown) \<and> 
  ltime s2 Ctrl' = DELAY'TIMEOUT \<and> \<not> (getVarBool s3 userAtTop' \<or> getVarBool s3 userAtBottom') \<or> 
  getPstate s2 Ctrl' = Ctrl'stuckState \<and> ltime s2 Ctrl' = SUSPENSION_TIME'TIMEOUT \<and>  \<not> getVarBool s2 moving') \<and> 
  \<not> getVarBool s3 alarmButton' \<and> \<not> getVarBool s3  stuck' \<and>
  (\<forall> s4 s5. toEnvP s4 \<and> toEnvP s5 \<and> 
  substate s3 s4 \<and> substate s4 s5 \<and> substate s5 s1 \<and> toEnvNum s4 s5 = 1 \<longrightarrow>
    getPstate s4 Ctrl' = Ctrl'motionless \<and> \<not> getVarBool s5 alarmButton' \<and> \<not> getVarBool s5 stuck' \<and>
    \<not> (getVarBool s5 userAtTop' \<or> getVarBool s5 userAtBottom'))) \<or>
  (\<forall> s4 s5. toEnvP s4 \<and> toEnvP s5 \<and>  
  substate s4 s5 \<and> substate s5 s1 \<and> toEnvNum s4 s5 = 1 \<longrightarrow>
    getPstate s4 Ctrl' = Ctrl'motionless \<and> \<not> getVarBool s5 alarmButton' \<and> \<not> getVarBool s5 stuck' \<and>
    \<not> (getVarBool s5 userAtTop' \<or> getVarBool s5 userAtBottom'))) \<and>

(\<forall> s1. toEnvP s1 \<and> substate s1 s\<and> getPstate s1 Ctrl' = Ctrl'goUp \<longrightarrow> 
  ltime s1 Ctrl' \<le> DELAY'TIMEOUT) \<and>

(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> getPstate s1 Ctrl' = Ctrl'goUp \<longrightarrow>
  (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s1 \<and> 
  toEnvNum s2 s3 = 1 \<and> toEnvNum s2 s1 = ltime s1 Ctrl' \<and>
  ((getPstate s2 Ctrl' = Ctrl'motionless \<or> getPstate s2 Ctrl' = Ctrl'goUp) \<and>
  (getVarBool s3 userAtTop' \<or> getVarBool s3 userAtBottom') \<or> 
  getPstate s2 Ctrl' = Ctrl'stuckState \<and> ltime s2 Ctrl' = SUSPENSION_TIME'TIMEOUT \<and>  getVarBool s2 moving' \<and>
   getVarBool s2 direction' = UP') \<and>
   \<not> getVarBool s3 alarmButton' \<and> \<not> getVarBool s3  stuck')) \<and>

(\<forall> s3. toEnvP s3 \<and>  substate s3 s \<and>
 getPstate s3 Ctrl' = Ctrl'goUp \<longrightarrow> 
  (\<forall> s1. toEnvP s1 \<and> substate s1 s3 \<and> toEnvNum s1 s3 < ltime s3 Ctrl'  \<longrightarrow>  
    getPstate s1 Ctrl' = Ctrl'goUp) \<and>
  (\<forall> s2. toEnvP s2 \<and> substate s2 s3 \<and> toEnvNum s2 s3 < ltime s3 Ctrl' - 1 \<longrightarrow> 
  \<not> getVarBool s2 alarmButton' \<and> \<not> getVarBool s2 stuck' \<and>
  \<not> (getVarBool s2 userAtTop' \<or> getVarBool s2 userAtBottom'))) \<and>

(\<forall> s1. toEnvP s1 \<and> substate s1 s\<and> getPstate s1 Ctrl' = Ctrl'goDown \<longrightarrow> 
  ltime s1 Ctrl' \<le> DELAY'TIMEOUT) \<and>

(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> getPstate s1 Ctrl' = Ctrl'goDown \<longrightarrow>
(\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s1 \<and> toEnvNum s2 s3 = 1 \<and> toEnvNum s2 s1 = ltime s1 Ctrl' \<and>
((getPstate s2 Ctrl' = Ctrl'motionless \<or> getPstate s2 Ctrl' = Ctrl'goDown) \<and>
 (getVarBool s3 userAtTop' \<or> getVarBool s3 userAtBottom') \<or> 
getPstate s2 Ctrl' = Ctrl'stuckState \<and> ltime s2 Ctrl' = SUSPENSION_TIME'TIMEOUT \<and>  getVarBool s2 moving' \<and>
 getVarBool s2 direction' = DOWN') \<and>
 \<not> getVarBool s3 alarmButton' \<and> \<not> getVarBool s3  stuck')) \<and>

(\<forall> s3.  toEnvP s3 \<and>  substate s3 s  \<and> getPstate s3 Ctrl' = Ctrl'goDown \<longrightarrow> 
(\<forall> s1. toEnvP s1 \<and> substate s1 s3 \<and> toEnvNum s1 s3 < ltime s3 Ctrl'  \<longrightarrow> getPstate s1 Ctrl' = Ctrl'goDown) \<and>
(\<forall> s2. toEnvP s2 \<and> substate s2 s3 \<and> toEnvNum s2 s3 < ltime s3 Ctrl' - 1 \<longrightarrow>
 \<not> getVarBool s2 alarmButton' \<and> \<not> getVarBool s2 stuck' \<and>
\<not> (getVarBool s2 userAtTop' \<or> getVarBool s2 userAtBottom'))) \<and>

(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> getPstate s1 Ctrl' = Ctrl'stuckState \<longrightarrow> 
  ltime s1 Ctrl' \<le> SUSPENSION_TIME'TIMEOUT) \<and>

(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> getPstate s1 Ctrl' = Ctrl'stuckState \<and> 
getVarBool s1 moving' \<and> getVarBool s1 direction' = UP' \<longrightarrow>
  (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s1 \<and> toEnvNum s2 s3 = 1 \<and>toEnvNum s2 s1 = ltime s1 Ctrl' \<and>
  (getPstate s2 Ctrl' = Ctrl'goUp \<or> getPstate s2 Ctrl' = Ctrl'stuckState \<and> getVarBool s2 moving' \<and> getVarBool s2 direction' = UP')
   \<and> \<not> getVarBool s3 alarmButton' \<and> getVarBool s3 stuck')) \<and>

(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> getPstate s1 Ctrl' = Ctrl'stuckState \<and> 
getVarBool s1 moving' \<and> getVarBool s1 direction' = DOWN' \<longrightarrow>
  (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s1 \<and> toEnvNum s2 s3 = 1 \<and>toEnvNum s2 s1 = ltime s1 Ctrl' \<and>
  (getPstate s2 Ctrl' = Ctrl'goDown \<or> getPstate s2 Ctrl' = Ctrl'stuckState \<and> getVarBool s2 moving' \<and>
   getVarBool s2 direction' = DOWN') \<and> \<not> getVarBool s3 alarmButton' \<and> getVarBool s3 stuck')) \<and>

(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> 
getPstate s1 Ctrl' = Ctrl'stuckState \<and> \<not> getVarBool s1 moving' \<longrightarrow>
  (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s1 \<and> toEnvNum s2 s3 = 1 \<and>toEnvNum s2 s1 = ltime s1 Ctrl' \<and>
  (getPstate s2 Ctrl' = Ctrl'motionless \<or> getPstate s2 Ctrl' = Ctrl'stuckState \<and> \<not> getVarBool s2 moving') \<and> \<not> getVarBool s3 alarmButton' \<and> getVarBool s3 stuck'))\<and>

(\<forall> s3. toEnvP s3 \<and> substate s3 s \<and>  getPstate s3 Ctrl' = Ctrl'stuckState \<longrightarrow>
  (\<forall> s1. toEnvP s1 \<and> substate s1 s3 \<and> toEnvNum s1 s3 < ltime s3 Ctrl' \<longrightarrow> 
    getPstate s1 Ctrl' = Ctrl'stuckState \<and>
    getVarBool s1 moving' = getVarBool s3 moving' \<and> 
    getVarBool s1 direction' = getVarBool s3 direction')  \<and>
  (\<forall> s2. toEnvP s2 \<and> substate s2 s3 \<and> toEnvNum s2 s3 < ltime s3 Ctrl' - 1 \<longrightarrow>
    \<not> getVarBool s2 alarmButton' \<and> \<not> getVarBool s2 stuck' \<and> 
    getVarBool s2 up' = False \<and> getVarBool s2 down' = False)) \<and>

(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> getPstate s1 Ctrl' = Ctrl'emergency \<longrightarrow>
  (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s1 \<and> 
  toEnvNum s2 s3 = 1 \<and> getPstate s2 Ctrl' \<noteq> Ctrl'emergency \<and>
  getVarBool s3 alarmButton' \<and>
  (\<forall> s4 s5. toEnvP s4 \<and> toEnvP s5 \<and> substate s3 s4 \<and> 
  substate s4 s5 \<and> substate s5 s1 \<and> toEnvNum s4 s5 = 1 \<longrightarrow>
    getPstate s4 Ctrl' = Ctrl'emergency \<and> getVarBool s5 up' = False \<and> 
    getVarBool s5 down' = False))) \<and>

(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> getPstate s1 Ctrl' = Ctrl'motionless \<longrightarrow>
 getVarBool s1 up' = False \<and> getVarBool s1 down' = False \<and> \<not> getVarBool s1 moving') \<and>

(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> getPstate s1 Ctrl' = Ctrl'goUp \<longrightarrow>
getVarBool s1 up' = True \<and> getVarBool s1 down' = False \<and> getVarBool s1 moving' \<and> getVarBool s1 direction'= UP') \<and>

(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> getPstate s1 Ctrl' = Ctrl'goDown \<longrightarrow>
getVarBool s1 up' = False \<and> getVarBool s1 down' = True \<and> getVarBool s1 moving' \<and> getVarBool s1 direction'= DOWN') \<and>

(\<forall> s1. toEnvP s1 \<and> substate s1 s \<longrightarrow>
 getPstate s1 Ctrl' = Ctrl'motionless \<or> getPstate s1 Ctrl' = Ctrl'goUp \<or> getPstate s1 Ctrl' = Ctrl'goDown \<or>
getPstate s1 Ctrl' = Ctrl'stuckState \<or> getPstate s1 Ctrl' = Ctrl'emergency)
"


end