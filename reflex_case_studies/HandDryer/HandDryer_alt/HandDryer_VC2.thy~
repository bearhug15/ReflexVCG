theory HandDryer_VC2
imports HandDryerTheory
begin

lemma extra:
 assumes base_inv:"(extraInv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Wait''"
 and st1_if:"(getVarBool st1 ''inp_1'')=False"
 and st2:"(st2=(toEnv st1))"
shows "(extraInv st2)" by sorry
  (*using assms extraInv_def apply auto
  done*)

lemma
 assumes base_inv:"(inv1 st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Wait''"
 and st1_if:"(getVarBool st1 ''inp_1'')=False"
 and st2:"(st2=(toEnv st1))"
 and st_final:"st_final = st2"
shows "(inv1 st_final)"
proof (simp add: inv1_def; intro conjI)
  show "extraInv st_final" using assms extra inv1_def by simp
next
  show "R1_full st_final"
  proof -
    have 0:"toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms inv1_def extraInv_def by auto
    moreover have "P8_cons R1_A R1_B R1_C st0" using base_inv inv1_def R1_to_P8 by auto
    moreover have "( R1_A st_final st_final st0 \<longrightarrow> ( R1_B st_final st0 st_final \<and>  R1_C st_final st_final st_final))"
      using 0 substate_refl substate_asym toEnvNum_id assms R1_A_def R1_B_def R1_C_def  SMT.verit_bool_simplify(4)
      by (metis getVarBool.simps(2) getVarBool.simps(3) getVarBool.simps(7) linordered_nonzero_semiring_class.zero_le_one)
    moreover have "(\<forall>x. toEnvP x \<and> substate x st0 \<and> toEnvP (predEnv x) \<and> \<not>R1_A st0 x (predEnv x) \<and> R1_A st_final x (predEnv x)\<longrightarrow>
                    (\<exists> s4. toEnvP s4 \<and> substate x s4  \<and> substate s4 st_final \<and> R1_B x (predEnv x) s4 \<and>
                     (\<forall>s3. toEnvP s3 \<and> substate x s3 \<and> substate s3 s4 \<longrightarrow> R1_C x s4 s3)))"
      apply(auto simp only: R1_A_def)
      done
    ultimately show ?thesis using P8_lemma P8_to_R1 by blast
  qed
qed

lemma
 assumes base_inv:"(inv2 st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Wait''"
 and st1_if:"(getVarBool st1 ''inp_1'')=False"
 and st2:"(st2=(toEnv st1))"
 and st_final:"st_final = st2"
shows "(inv2 st_final)"
proof (simp add: inv2_def; intro conjI)
  show "extraInv st_final" using extra assms inv2_def by auto
next
  show "R2_full st_final"
  proof -
    have "toEnvP st0 \<and> toEnvP st_final \<and> st0=predEnv st_final" using assms inv2_def extraInv_def by auto
    moreover have "P2_1_cons R2_A st0" using assms inv2_def R2_to_P2_1 by auto
    moreover have "R2_A st_final st0" using assms R2_A_def by simp
    ultimately show ?thesis using P2_1_to_R2 P2_1_lemma by blast
  qed
qed

lemma
 assumes base_inv:"(inv3 st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Wait''"
 and st1_if:"(getVarBool st1 ''inp_1'')=False"
 and st2:"(st2=(toEnv st1))"
 and st_final:"st_final=st2"
shows "(inv3 st_final)"
proof (simp add: inv3_def; intro conjI)
  show "extraInv st_final" using assms extra inv3_def by simp
next
  have extra1:"R3_full st0 \<and> extraInv st0" using assms inv3_def by auto
  show "R3_full st_final"
  proof -
    have 0:"toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms inv3_def extraInv_def by auto
    moreover have "P8_cons R3_A R3_B R3_C st0" using extra1 R3_to_P8 by auto
    moreover have "(R3_A st_final st_final st0 \<longrightarrow> (R3_B st_final st0 st_final \<and> R3_C st_final st_final st_final))"
      apply (simp only: R3_A_def R3_B_def R3_C_def)
      using assms by (metis bot_nat_0.extremum_unique toEnvNum_id zero_neq_numeral)
    moreover have  "(\<forall>x. toEnvP x \<and> substate x st0 \<and> toEnvP (predEnv x) \<and> \<not>R3_A st0 x (predEnv x) \<and> R3_A st_final x (predEnv x)\<longrightarrow>
          (\<exists> s4. toEnvP s4 \<and> substate x s4  \<and> substate s4 st_final \<and> R3_B x (predEnv x) s4 \<and>
           (\<forall>s3. toEnvP s3 \<and> substate x s3 \<and> substate s3 s4 \<longrightarrow> R3_C x s4 s3)))"
    proof(simp only: R3_A_def R3_B_def R3_C_def SMT.verit_bool_simplify(4); rule allI; rule impI)
      fix x
      assume "toEnvP x \<and>
         substate x st0 \<and>
         toEnvP (predEnv x) \<and>
         \<not> (20 \<le> toEnvNum x st0 \<and>
             getVarBool (predEnv x) ''inp_1'' =
             True \<and>
             getVarBool (predEnv x) ''out_1'' =
             True \<and>
             getVarBool x ''inp_1'' = False) \<and>
         20 \<le> toEnvNum x st_final \<and>
         getVarBool (predEnv x) ''inp_1'' = True \<and>
         getVarBool (predEnv x) ''out_1'' = True \<and>
         getVarBool x ''inp_1'' = False"
      moreover have "toEnvNum x st_final = toEnvNum x st0 + 1" 
        using 0 predEnv_toEnvNum predEnv_substate toEnvNum3 calculation(1) by force
      ultimately have prems:"toEnvP x \<and>
          substate x st0 \<and>
          toEnvP (predEnv x) \<and>
          getVarBool (predEnv x) ''inp_1'' = True \<and>
          getVarBool (predEnv x) ''out_1'' = True \<and>
          getVarBool x ''inp_1'' = False \<and>
          toEnvNum x st0 = 19 \<and>
          toEnvNum x st_final = 20" using 0  by auto

      show "\<exists>s4. toEnvP s4 \<and> substate x s4 \<and>substate s4 st_final \<and>
              (toEnvNum x s4 \<le> 20 \<and>
               (getVarBool s4 ''out_1'' = False \<or>
                getVarBool s4 ''inp_1'' = True)) \<and>
              (\<forall>s3. (toEnvP s3 \<and> substate x s3 \<and> substate s3 s4) \<and> s3 \<noteq> s4 \<longrightarrow>
                    getVarBool s3 ''out_1'' = True \<and>
                    getVarBool s3 ''inp_1'' = False)"
      proof -
        have 1:"getPstate st0 ''Dryer'' = ''Wait''" using assms by auto
        then have 2:"getVarBool st0 ''out_1'' = False" using extra1 extraInv_def 0 substate_refl by auto
        have 3:"substate (predEnv x) st0" using prems predEnv_substate substate_trans by blast
        then have 4:"getPstate (predEnv x) ''Dryer'' = ''Work''" using 3 prems extra1 extraInv_def by blast
        consider (a)"(\<forall> s2. toEnvP s2 \<and> substate s2 st0 \<longrightarrow> 
                  getPstate s2 ''Dryer'' = ''Wait'')"
          | (b)"(\<exists> s2. toEnvP s2 \<and> substate s2 st0 \<and>
                getPstate s2 ''Dryer'' = ''Work'' \<and> ltime s2 ''Dryer'' \<ge>2000 \<and>
                (\<forall> s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 st0 \<and>
                  s3\<noteq>s2 \<longrightarrow> getPstate s3 ''Dryer'' = ''Wait''))" using extra1 extraInv_def extra2_def 1 substate_refl 0 by force
        thus ?thesis
        proof (cases)
          case a
          have "toEnvP (predEnv x) \<and> substate (predEnv x) st0" using prems 3 by auto
          then have "getPstate (predEnv x) ''Dryer'' = ''Wait''" using a by auto
          then show ?thesis using 2 3 4 prems by auto
        next
          case b
          then obtain s2 where o1:
            "toEnvP s2 \<and>
             substate s2 st0 \<and>
             getPstate s2 ''Dryer'' = ''Work'' \<and>
             2000 \<le> ltime s2 ''Dryer'' \<and>
             (\<forall>s3. toEnvP s3 \<and>
                   substate s2 s3 \<and> substate s3 st0 \<and> s3 \<noteq> s2 \<longrightarrow>
                   getPstate s3 ''Dryer'' = ''Wait'')" by auto
          then consider (ba)"substate (predEnv x) s2"
              | (bb)"substate s2 (predEnv x) \<and> s2\<noteq>(predEnv x)" using 3 substate_total by auto
          then show ?thesis 
          proof cases
            case ba
            have 5:"toEnvNum (predEnv x) st0 = 20" using prems toEnvNum3 predEnv_substate predEnv_toEnvNum by force
            have sub5:"s2 \<noteq> st0" using o1 1 by auto
            then have "toEnvNum s2 st0 \<ge> 1" using o1 0 substate_toEnvNum_id by auto  
            then have 6:"toEnvNum (predEnv x) s2 <20" using toEnvNum3 ba o1 5 by auto
            have 7:"0 < ltime s2 ''Dryer'' \<and> ltime s2 ''Dryer'' \<le> 2000 \<and>
                        (\<forall> s1. toEnvP s1 \<and> substate s1 s2 \<and>
                         (toEnvNum s1 s2+1) = ltime s2 ''Dryer'' div 100 \<longrightarrow>
                        getVarBool s1 ''inp_1'' = True \<and>
                         getVarBool s1 ''out_1'' = True) \<and>
                        (\<forall> s1. toEnvP s1 \<and> substate s1 s2 \<and>
                         (toEnvNum s1 s2+1) < ltime s2 ''Dryer'' div 100 \<longrightarrow>
                        getVarBool s1 ''inp_1'' = False \<and>
                         getVarBool s1 ''out_1'' = True)" using extra1 extraInv_def extra1_def o1 by force
            then have "ltime s2 ''Dryer'' div 100 = 20" using o1 by auto
            then consider (b1)"toEnvNum (predEnv x) s2 +1 <ltime s2 ''Dryer'' div 100"
                | (b2)"toEnvNum (predEnv x) s2 +1 = ltime s2 ''Dryer'' div 100" using 7 6 by force
            then show ?thesis 
            proof cases
              case b1
              then have "getVarBool (predEnv x) ''inp_1'' = False" using 7 ba prems by blast
              then show ?thesis  using prems by auto
            next
              case b2
              then have "\<forall>s1. toEnvP s1 \<and> substate s1 s2 \<and> substate (predEnv x) s1 \<and> (predEnv x)\<noteq>s1 \<longrightarrow>
                    (toEnvNum s1 s2+1) < ltime s2 ''Dryer'' div 100" using 7 o1 prems shift_toEnvNum substate_asym substate_shift substate_trans
                by (metis (mono_tags, lifting) add_le_imp_le_right verit_comp_simplify1(3))
              then have 8:"\<forall>s1. toEnvP s1 \<and> substate s1 s2 \<and> substate x s1 \<longrightarrow>
                          getVarBool s1 ''inp_1'' = False \<and> getVarBool s1 ''out_1'' = True" 
                using 7 prems predEnv_substate substate_asym substate_trans
                by (smt (verit))
              have sub8:"toEnvP s2 \<and> toEnvP st0 \<and> substate s2 st0 \<and>  s2 \<noteq> st0" using o1 sub5 0 by auto
              then have "\<exists> s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 st0 \<and>s2\<noteq>s3" using substate_eq_or_predEnv substate_refl by auto
              then have "\<exists> s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 st0 \<and>s2=(predEnv s3)" using predEnv_substate next_state_exists sub8 by blast
              then obtain s3 where o2: "toEnvP s3 \<and> substate s3 st0 \<and> s2 = predEnv s3 " by auto
              then have "getPstate s3 ''Dryer'' = ''Wait''" using o1
                by (metis predEnv_substate predEnv_toEnvNum toEnvNum_id zero_neq_one)
              then have 9:"getVarBool s3 ''out_1'' = False" using o2 extra1 extraInv_def by auto
              have 10: "substate x s3" proof -
                have "substate s2 s3 \<and> s2\<noteq>s3" using o2 predEnv_substate prems o1
                  by (metis predEnv_toEnvNum toEnvNum_id zero_neq_one)
                thus ?thesis using ba prems substate_asym substate_trans o2 predEnv_substate_imp_eq_or_substate by blast 
              qed
              have 11: "toEnvNum x s3 \<le> 20" using prems 10 o2 toEnvNum_leq
                by (metis \<open>toEnvNum x st_final = toEnvNum x st0 + 1\<close> le_add1)
              then have "toEnvP s3 \<and> substate x s3 \<and>substate s3 st_final \<and>
                        (toEnvNum x s3 \<le> 20 \<and>
                         (getVarBool s3 ''out_1'' = False \<or>
                          getVarBool s3 ''inp_1'' = True))" using o1 prems o2 9 10 11 0 predEnv_substate substate_trans by blast
              moreover have "(\<forall>s4. (toEnvP s4 \<and> substate x s4 \<and> substate s4 s3) \<and> s3 \<noteq> s4 \<longrightarrow>
                              getVarBool s4 ''out_1'' = True \<and>
                              getVarBool s4 ''inp_1'' = False)"
                using "8" o2 substate_eq_or_predEnv by auto
              ultimately show ?thesis by auto
            qed
          next
            case bb
            then have "getPstate (predEnv x) ''Dryer'' = ''Wait''" using prems 3 o1 by blast
            then show ?thesis using 4 by auto  
          qed
        qed
      qed
    qed
    ultimately show ?thesis using P8_lemma P8_to_R3 by auto
  qed
qed

lemma
 assumes base_inv:"(inv4 st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Wait''"
 and st1_if:"(getVarBool st1 ''inp_1'')=False"
 and st2:"(st2=(toEnv st1))"
 and st_final:"st_final = st2"
shows "(inv4 st_final)" 
proof (simp add: inv4_def; intro conjI)
  show "extraInv st_final" using assms extra inv4_def by simp
next
  have extra1:"R4_full st0 \<and> extraInv st0" using assms inv4_def by auto
  show "R4_full st_final"
  proof -
    have "toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms extra1 extraInv_def by auto
    moreover have "P2_1_cons R4_A st0" using extra1 R4_to_P2_1 by auto
    moreover have "R4_A st_final st0" using R4_A_def assms by auto
    ultimately show ?thesis using P2_1_lemma P2_1_to_R4 by auto
  qed
qed

end
