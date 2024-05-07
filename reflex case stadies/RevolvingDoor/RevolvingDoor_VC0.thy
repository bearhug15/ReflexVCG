theory RevolvingDoor_VC0
imports RevolvingDoorTheory
begin

lemma extra:
 assumes st0:"(st0=emptyState)"
 and st1:"(st1=(setPstate st0 ''Controller'' ''motionless''))"
 and st2:"(st2=(toEnv st1))"
 and st_final:"(st_final=st2)"
shows "(extraInv st_final)"
  using extraInv_def assms apply auto
  done

lemma
 assumes st0:"(st0=emptyState)"
 and st1:"(st1=(setPstate st0 ''Controller'' ''motionless''))"
 and st2:"(st2=(toEnv st1))"
 and st_final:"(st_final=st2)"
shows "(inv1 st_final)"
  including R1_defs using inv1_def assms extra by auto

lemma
 assumes st0:"(st0=emptyState)"
 and st1:"(st1=(setPstate st0 ''Controller'' ''motionless''))"
 and st2:"(st2=(toEnv st1))"
 and st_final:"(st_final=st2)"
shows "(inv2 st_final)"
  including R2_defs using inv2_def assms extra by auto

lemma
 assumes st0:"(st0=emptyState)"
 and st1:"(st1=(setPstate st0 ''Controller'' ''motionless''))"
 and st2:"(st2=(toEnv st1))"
 and st_final:"(st_final=st2)"
shows "(inv3 st_final)"
  including R3_defs using inv3_def assms extra by auto

lemma
 assumes st0:"(st0=emptyState)"
 and st1:"(st1=(setPstate st0 ''Controller'' ''motionless''))"
 and st2:"(st2=(toEnv st1))"
 and st_final:"(st_final=st2)"
shows "(inv4 st_final)"
  including R4_defs using inv4_def assms extra by auto

lemma
 assumes st0:"(st0=emptyState)"
 and st1:"(st1=(setPstate st0 ''Controller'' ''motionless''))"
 and st2:"(st2=(toEnv st1))"
 and st_final:"(st_final=st2)"
shows "(inv5 st_final)"
  including R5_defs using inv5_def assms extra by auto

lemma
 assumes st0:"(st0=emptyState)"
 and st1:"(st1=(setPstate st0 ''Controller'' ''motionless''))"
 and st2:"(st2=(toEnv st1))"
 and st_final:"(st_final=st2)"
shows "(inv6 st_final)"
  including R6_defs using inv6_def assms extra by auto

end
