theory Requirements
imports ReflexPatterns extra
begin

definition R1 where "R1 s \<equiv> toEnvP s \<and>
(\<forall> s1 s2. substate s1 s2 \<and> substate s2 s \<and> 
  toEnvP s1 \<and> toEnvP s2 \<and> toEnvNum s1 s2 = 1 \<and>
  getPstate s1 ''Ctrl'' \<noteq>''stuckState''\<and> getPstate s1 ''Ctrl'' \<noteq>''emergency'' \<and>
  \<not> getVarBool s1 ''out_0'' \<and> \<not> getVarBool s1 ''out_1'' \<and> 
  getVarBool s2 ''inp_0'' \<and> getVarBool s2 ''inp_2'' = True \<and> 
  \<not> getVarBool s2 ''inp_3'' \<and> \<not> getVarBool s2 ''inp_4'' \<longrightarrow>
    getVarBool s2 ''out_0'' = True)"

definition R1A where
"R1A s2 s1 \<equiv>
  getPstate s1 ''Ctrl'' \<noteq>''stuckState''\<and> getPstate s1 ''Ctrl'' \<noteq>''emergency'' \<and>
  \<not> getVarBool s1 ''out_0'' \<and> \<not> getVarBool s1 ''out_1'' \<and> 
  getVarBool s2 ''inp_0'' \<and> getVarBool s2 ''inp_2'' = True \<and> 
  \<not> getVarBool s2 ''inp_3'' \<and> \<not> getVarBool s2 ''inp_4'' \<longrightarrow>
    getVarBool s2 ''out_0'' = True"

lemma R1_to_P2_1_cons:
"R1 s \<Longrightarrow> P2_1_cons R1A s"
  apply (auto simp add: R1_def P2_1_cons_def R1A_def)
  done

lemma P2_1_cons_to_R1:
"toEnvP s \<and> P2_1_cons R1A s \<Longrightarrow> R1 s"
  apply (auto simp add: R1_def P2_1_cons_def R1A_def)
  done

definition "inv1 s \<equiv> R1 s \<and> extraInv s"


definition R2 where "R2 s\<equiv> toEnvP s \<and>
(\<forall> s1 s2. substate s1 s2 \<and> substate s2 s \<and> 
  toEnvP s1 \<and> toEnvP s2 \<and> toEnvNum s1 s2 = 1 \<and> 
  toEnvNum s2 s \<ge> 120000 div 100 \<and>
  (getVarBool s1 ''out_0'' = True \<or> getVarBool s1 ''out_1'' = True) \<and> 
  \<not> getVarBool s2 ''inp_0'' \<and> \<not> getVarBool s2 ''inp_1'' \<longrightarrow>
    (\<exists> s4. toEnvP s4 \<and> substate s2 s4 \<and> substate s4 s \<and> 
      toEnvNum s2 s4 \<le> 120000 div 100 \<and>
      (getVarBool s4 ''out_0'' = False \<and> getVarBool s4 ''out_1'' = False \<or> 
      getVarBool s4 ''inp_0'' \<or> getVarBool s4 ''inp_1'') \<and>
      (\<forall> s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s4 \<and> s3 \<noteq> s4 \<longrightarrow>
        (getVarBool s3 ''out_0'' = True \<or> getVarBool s3 ''out_1'' = True) \<and> 
        \<not> getVarBool s3 ''inp_0'' \<and> \<not> getVarBool s3 ''inp_1'')))"

definition R2A where 
"R2A s s2 s1 \<equiv>
  toEnvNum s2 s \<ge> 120000 div 100 \<and>
  (getVarBool s1 ''out_0'' = True \<or> getVarBool s1 ''out_1'' = True) \<and> 
  \<not> getVarBool s2 ''inp_0'' \<and> \<not> getVarBool s2 ''inp_1''"

definition R2B where
"R2B s2 s1 s4\<equiv> 
  toEnvNum s2 s4 \<le> 120000 div 100 \<and>
  (getVarBool s4 ''out_0'' = False \<and> getVarBool s4 ''out_1'' = False \<or> 
  getVarBool s4 ''inp_0'' \<or> getVarBool s4 ''inp_1'')"

definition R2C where
"R2C s2 s4 s3 \<equiv>
  s3 \<noteq> s4 \<longrightarrow>
  (getVarBool s3 ''out_0'' = True \<or> getVarBool s3 ''out_1'' = True) \<and> 
  \<not> getVarBool s3 ''inp_0'' \<and> \<not> getVarBool s3 ''inp_1''"

lemma R2_to_P8_cons:
"R2 s \<Longrightarrow> P8_cons R2A R2B R2C s"
  apply (auto simp add: R2_def P8_cons_def R2A_def R2B_def R2C_def SMT.verit_bool_simplify(4))
  done

lemma P8_cons_to_R2:
"toEnvP s \<and> P8_cons R2A R2B R2C s \<Longrightarrow>R2 s"
  apply (auto simp add: R2_def P8_cons_def R2A_def R2B_def R2C_def SMT.verit_bool_simplify(4))
  done

definition "inv2 s \<equiv> R2 s \<and> extraInv s"


definition R3 where "R3 s \<equiv> toEnvP s \<and>
(\<forall> s1 s2. substate s1 s2 \<and> substate s2 s \<and> 
  toEnvP s1 \<and> toEnvP s2 \<and> toEnvNum s1 s2 = 1 \<and>
  getVarBool s1 ''out_0'' = True \<and> 
  (getVarBool s2 ''inp_0'' \<or>  getVarBool s2 ''inp_1'') \<and>
  \<not> getVarBool s1 ''inp_3'' \<and> \<not> getVarBool s1 ''inp_4'' \<and> 
  \<not> getVarBool s2 ''inp_3'' \<and> \<not> getVarBool s2 ''inp_4'' \<longrightarrow>
    getVarBool s2 ''out_0'' = True)"

definition R3A where
"R3A s2 s1 \<equiv>
  getVarBool s1 ''out_0'' = True \<and> 
  (getVarBool s2 ''inp_0'' \<or>  getVarBool s2 ''inp_1'') \<and>
  \<not> getVarBool s1 ''inp_3'' \<and> \<not> getVarBool s1 ''inp_4'' \<and> 
  \<not> getVarBool s2 ''inp_3'' \<and> \<not> getVarBool s2 ''inp_4'' \<longrightarrow>
    getVarBool s2 ''out_0'' = True"

lemma R3_to_P2_1_cons:
"R3 s \<Longrightarrow> P2_1_cons R3A s"
  apply (auto simp add: R3_def P2_1_cons_def R3A_def)
  done

lemma P2_1_cons_to_R3:
"toEnvP s \<and> P2_1_cons R3A s \<Longrightarrow> R3 s"
  apply (auto simp add: R3_def P2_1_cons_def R3A_def)
  done

definition "inv3 s \<equiv> R3 s \<and> extraInv s"

definition R4 where "R4 s \<equiv> toEnvP s \<and>
(\<forall> s1 s2 s3. toEnvP s1 \<and> toEnvP s2 \<and> toEnvP s3 \<and> 
  substate s1 s2 \<and> substate s2 s3 \<and> substate s3 s \<and>
  toEnvNum s1 s2 = 1 \<and> 1 \<le> toEnvNum s2 s3 \<and> toEnvNum s2 s3 < 1000 div 100 \<and>
  (getVarBool s1 ''out_0'' = True \<or> getVarBool s1 ''out_1'' = True) \<and> 
  getVarBool s2 ''inp_4'' \<longrightarrow>
    getVarBool s3 ''out_0'' = False \<and> getVarBool s3 ''out_1'' = False)"

definition R4A where 
"R4A s3 s2 s1 \<equiv>
  1 \<le> toEnvNum s2 s3 \<and> toEnvNum s2 s3 < 1000 div 100 \<and>
  (getVarBool s1 ''out_0'' = True \<or> getVarBool s1 ''out_1'' = True) \<and> 
  getVarBool s2 ''inp_4'' \<longrightarrow>
    getVarBool s3 ''out_0'' = False \<and> getVarBool s3 ''out_1'' = False"

lemma P3_cons_to_R4:
"(toEnvP s \<and> P3_cons R4A s) = R4 s"
  apply (auto simp add: P3_cons_def R4A_def R4_def)
  done

lemma R4_to_P3_cons:
"R4 s \<Longrightarrow> P3_cons R4A s"
  using P3_cons_to_R4 by auto


definition "inv4 s \<equiv> R4 s \<and> extraInv s"


definition R5 where "R5 s \<equiv> toEnvP s \<and>
(\<forall> s1 s2. substate s1 s2 \<and> substate s2 s \<and> 
  toEnvP s1 \<and> toEnvP s2 \<and> toEnvNum s1 s2 = 1 \<and> toEnvNum s2 s \<ge> 1 \<and>
  (getVarBool s1 ''out_0'' = True \<or> getVarBool s1 ''out_1'' = True) \<and> 
  getVarBool s2 ''inp_3'' \<longrightarrow>
    (\<exists> s4. toEnvP s4 \<and> substate s2 s4 \<and> substate s4 s \<and> 
      toEnvNum s2 s4 \<le> 1 \<and> getVarBool s4 ''out_0'' = False \<and> 
      getVarBool s4 ''out_1'' = False \<and>
      (\<forall> s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s4 \<and> s3 \<noteq> s4 \<longrightarrow>
        getVarBool s3 ''out_0'' = True \<or> getVarBool s3 ''out_1'' = True)))"

definition R5A where
"R5A s s2 s1 \<equiv>
  toEnvNum s2 s \<ge> 1 \<and>
  (getVarBool s1 ''out_0'' = True \<or> getVarBool s1 ''out_1'' = True) \<and> 
  getVarBool s2 ''inp_3''"

definition R5B where
"R5B s2 s1 s4 \<equiv>
  toEnvNum s2 s4 \<le> 1 \<and> getVarBool s4 ''out_0'' = False \<and> 
  getVarBool s4 ''out_1'' = False"

definition R5C where
"R5C s2 s4 s3 \<equiv>
  s3 \<noteq> s4 \<longrightarrow>
  getVarBool s3 ''out_0'' = True \<or> getVarBool s3 ''out_1'' = True"

lemma R5_to_P8_cons:
"R5 s \<Longrightarrow> P8_cons R5A R5B R5C s"
  apply (auto simp add: R5_def P8_cons_def R5A_def R5B_def R5C_def SMT.verit_bool_simplify(4))
  done

lemma P8_cons_to_R5:
"toEnvP s \<and> P8_cons R5A R5B R5C s \<Longrightarrow>R5 s"
  apply (auto simp add: R5_def P8_cons_def R5A_def R5B_def R5C_def SMT.verit_bool_simplify(4))
  done

definition "inv5 s \<equiv> R5 s \<and> extraInv s"

end