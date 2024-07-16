theory HandDryer_VC0
imports HandDryerTheory
begin

lemma
 assumes s0:"(s0=emptyState)"
 and s1:"(s1=(setPstate s0 ''Dryer'' ''Wait''))"
 and s2:"(s2=(toEnv s1))"
shows "(inv1 s2)"
  including R1_defs apply (simp add: assms inv1_def  extraInv_def)
  done

lemma
 assumes s0:"(s0=emptyState)"
 and s1:"(s1=(setPstate s0 ''Dryer'' ''Wait''))"
 and s2:"(s2=(toEnv s1))"
shows "(inv2 s2)"
  apply (simp add: assms inv2_def R2_def R2_sub1_def R2_sub1_prems_def extraInv_def)
  done

lemma
 assumes s0:"(s0=emptyState)"
 and s1:"(s1=(setPstate s0 ''Dryer'' ''Wait''))"
 and s2:"(s2=(toEnv s1))"
shows "(inv3 s2)"
  apply (simp add: assms inv3_def R3_def R3_sub1_def R3_sub1_prems_def extraInv_def)
  done

lemma
 assumes s0:"(s0=emptyState)"
 and s1:"(s1=(setPstate s0 ''Dryer'' ''Wait''))"
 and s2:"(s2=(toEnv s1))"
shows "(inv4 s2)"
  apply (simp add: assms inv4_def R4_def R4_sub1_def extraInv_def)
  done

lemma
 assumes s0:"(s0=emptyState)"
 and s1:"(s1=(setPstate s0 ''Dryer'' ''Wait''))"
 and s2:"(s2=(toEnv s1))"
shows "(inv5 s2)"
  apply (simp add: assms inv5_def R5_def R5_sub1_def R5_sub1_prems_def extraInv_def)
  done

end
