theory HandDryer_VC1
imports HandDryerTheory
begin

lemma extra:
 assumes base_inv:"(extraInv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Wait''"
 and st1_if:"(getVarBool st1 ''inp_1'')=True"
 and st2:"(st2=(setVarBool st1 ''out_1'' True))"
 and st3:"(st3=(setPstate st2 ''Dryer'' ''Work''))"
 and st4:"(st4=(toEnv st3))"
shows "(extraInv st4)"
  using assms extraInv_def extra1_def extra2_def apply sorry
  done


lemma
 assumes base_inv:"(inv1 st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Wait''"
 and st1_if:"(getVarBool st1 ''inp_1'')=True"
 and st2:"(st2=(setVarBool st1 ''out_1'' True))"
 and st3:"(st3=(setPstate st2 ''Dryer'' ''Work''))"
 and st4:"(st4=(toEnv st3))"
<<<<<<< HEAD
 and st_final:"st_final = st4"
=======
and st_final:"st_final = st4"
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
shows "(inv1 st_final)"
proof (simp add: inv1_def; intro conjI)
  show "extraInv st_final" using assms extra inv1_def by simp
next
  show "R1_full st_final"
  proof -
    have 0:"toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms inv1_def extraInv_def by auto
<<<<<<< HEAD
    moreover have "P8_cons R1_A R1_B R1_C st0" using base_inv inv1_def R1_to_P8 by auto
    moreover have "( R1_A st_final st_final st0 \<longrightarrow> ( R1_B st_final st0 st_final \<and>  R1_C st_final st_final st_final))"
      apply (simp only: R1_A_def R1_B_def R1_C_def  SMT.verit_bool_simplify(4)) 
      using 0 substate_refl substate_asym toEnvNum_id assms 
      by (metis getVarBool.simps(2) getVarBool.simps(3) getVarBool.simps(7) linordered_nonzero_semiring_class.zero_le_one)
    moreover have "(\<forall>x. toEnvP x \<and> substate x st0 \<and> toEnvP (predEnv x) \<and> \<not>R1_A st0 x (predEnv x) \<and> R1_A st_final x (predEnv x)\<longrightarrow>
                    (\<exists> s4. toEnvP s4 \<and> substate x s4  \<and> substate s4 st_final \<and> R1_B x (predEnv x) s4 \<and>
                     (\<forall>s3. toEnvP s3 \<and> substate x s3 \<and> substate s3 s4 \<longrightarrow> R1_C x s4 s3)))"
      apply(auto simp only: R1_A_def)
      done
    ultimately show ?thesis using P8_lemma P8_to_R1 by blast
=======
    moreover have "P8_cons R1_A R1_B R1_C st0" using base_inv inv1_def R1_A by auto
    moreover have "(P8_ABC_comb R1_A R1_B R1_C) st_final st_final"
      apply (simp only: P8_ABC_comb R1_A_def R1_B_def R1_C_def p_2_3_conpred_def p_1_2_conpred_def P8_BC_comb AB_comb_after_def SMT.verit_bool_simplify(4)) 
      using 0 substate_refl substate_asym toEnvNum_id assms 
      by (metis getVarBool.simps(2) getVarBool.simps(3) getVarBool.simps(7) linordered_nonzero_semiring_class.zero_le_one)
    moreover have "(\<forall>x. toEnvP x \<and> substate x st_final \<and> (p_2_3_conpred R1_A) st_final x \<and> \<not> (p_2_3_conpred R1_A) st0 x \<longrightarrow> 
                      (\<exists>y. toEnvP y \<and> substate x y \<and> substate y st_final \<and> (P8_BC_comb R1_B R1_C) x y))"
      apply(simp only: p_2_3_conpred_def R1_A_def)
      apply blast
      done
    ultimately show ?thesis using P8_lemma A_R1 by blast
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
  qed
qed

lemma 
assumes base_inv:"(inv2 st0)"
 and st1:"st1=(setVarBool st0 ''inp_1'' inp_1)"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Wait''"
 and st1_if:"(getVarBool st1 ''inp_1'')=True"
 and st2:"(st2=(setVarBool st1 ''out_1'' True))"
 and st3:"(st3=(setPstate st2 ''Dryer'' ''Work''))"
 and st4:"(st4=(toEnv st3))"
 and st_final:"(st_final=st4)"
shows "(inv2 st_final)"
proof (simp add: inv2_def; intro conjI)
  show "extraInv st_final" using assms extra inv2_def by simp
next
  show "R2_full st_final"
  proof -
    have "toEnvP st0 \<and> toEnvP st_final \<and> st0=predEnv st_final" using assms inv2_def extraInv_def by auto
<<<<<<< HEAD
    moreover have "P2_1_cons R2_A st0" using assms inv2_def R2_to_P2_1 by auto
    moreover have "R2_A st_final st0" using assms R2_A_def by simp
    ultimately show ?thesis using P2_1_to_R2 P2_1_lemma by blast
=======
    moreover have "P2_1_cons R2_A st0" using assms inv2_def R2_A by auto
    moreover have "R2_A st_final st0" using assms R2_A_def by simp
    ultimately show ?thesis 
    using A_R2 P2_1_lemma by blast
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
  qed
qed

lemma 
assumes base_inv:"(inv3 st0)"
 and st1:"st1=(setVarBool st0 ''inp_1'' inp_1)"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Wait''"
 and st1_if:"(getVarBool st1 ''inp_1'')=True"
 and st2:"(st2=(setVarBool st1 ''out_1'' True))"
 and st3:"(st3=(setPstate st2 ''Dryer'' ''Work''))"
 and st4:"(st4=(toEnv st3))"
<<<<<<< HEAD
=======
shows "(inv3 st4)"
proof (simp add: inv3_def; intro conjI)
  show "extraInv st4" using assms extra inv3_def by simp
next
  show "R3 st4"
  proof (simp add: R3_def;intro conjI)
    show "toEnvP st4" using assms by simp
  next
    show "R3_sub1 st4"
    proof(simp only: R3_sub1_def; intro allI; rule impI)
      fix s1 s2
      assume prems:"R3_sub2_prems st4 s1 s2" 
      then obtain "20 \<le> toEnvNum s2 st4" using R3_sub2_prems_def by auto
      from le_imp_less_or_eq[OF this] show "R3_sub2 st4 s2"
        apply (rule disjE)
      proof -
        assume 1:"20 < toEnvNum s2 st4"
        have 2: "substate s2 st0" using assms prems R3_sub2_prems_def substate_eq_or_predEnv toEnvNum_id by (simp split:if_splits)
        obtain "R3_sub1 st0" using prems R3_def base_inv inv3_def by auto
        then have "R3_sub2 st0 s2" using 1 2 prems assms R3_sub2_def R3_sub2_prems_def R3_sub1_def by (auto simp add: split:if_splits)
        then obtain s4 where 3: "R3_sub3_prems st0 s2 s4 \<and> R3_sub3 s2 s4" using R3_sub2_def by auto
        then have "R3_sub3_prems st4 s2 s4 \<and> R3_sub3 s2 s4" using assms R3_sub3_prems_def by auto
        thus ?thesis using R3_sub2_def by auto
      next
        assume 1: "20 = toEnvNum s2 st4"
        have 2:"R3_sub3 s2 s2" using substate_asym R3_sub3_def R3_sub4_prems_def by auto
        show ?thesis
        proof -
          define s5::state where "s5=s2"
          have "toEnvP s5 \<and> substate s2 s5 \<and> substate s5 st4
                \<longrightarrow> pred3 st4 s2 s5" including pred3_defs
             proof(induction rule: state_down_ind)
               case 1
               then show ?case using prems assms R3_sub2_prems_def by auto
             next
               case 2
               then show ?case
                 apply(simp only: pred3_def)
               proof
                 assume 3:"pred3_sub1 s2 st4"
                 have "pred3_sub2_prems st4 s2 st4 st4 \<and> pred3_sub3 st4 st4" 
                 proof(rule conjI)
                   show "pred3_sub2_prems st4 s2 st4 st4" using 1 assms pred3_sub2_prems_def prems substate_refl by fastforce
                 next
                   show "pred3_sub3 st4 st4" using substate_asym pred3_sub3_def by blast
                 qed
                 thus "pred3_sub2 st4 s2 st4" using pred3_sub2_def by auto
               qed
             next
               case (3 s5)
               then show ?case 
                 apply (simp only: pred3_def)
               proof
                 from 3(3) 
                 have 4:"pred3_sub1 s2 s5 \<Longrightarrow> pred3_sub2 st4 s2 s5" using pred3_def by blast
                 assume 5: "pred3_sub1 s2 (predEnv s5)"
                 show "pred3_sub2 st4 s2 (predEnv s5)"
                 proof cases
                   assume 6: "getVarBool (predEnv s5) ''out_1'' = False \<or>
                              getVarBool (predEnv s5) ''inp_1'' = True"
                   have 7: "substate (predEnv s5) st4" using predEnv_substate substate_trans 3 by blast
                   have 8: "substate s2 (predEnv s5)" using 3(2) substate_eq_or_predEnv prems R3_sub2_prems_def by auto
                   have "pred3_sub2_prems st4 s2 (predEnv s5) (predEnv s5) \<and> pred3_sub3 (predEnv s5) (predEnv s5)"
                   proof -
                     have "toEnvP (predEnv s5)" using predEnvP_or_emptyState[of s5] 
                     proof
                       assume "toEnvP (predEnv s5)"
                       thus ?thesis by assumption
                     next 
                       assume "predEnv s5 = emptyState"
                       thus ?thesis using 8 prems R3_sub2_prems_def by (metis substate.simps(1))
                     qed
                     moreover have "substate (predEnv s5) (predEnv s5)" using substate_refl by auto
                     moreover have "substate (predEnv s5) st4" using 7 by assumption
                     moreover have "toEnvNum s2 (predEnv s5) \<le> 20" using 1 7 8 toEnvNum3 by auto
                     moreover have "getVarBool (predEnv s5) ''out_1'' = False \<or>
                                    getVarBool (predEnv s5) ''inp_1'' = True" using 6 by assumption
                     moreover have "pred3_sub3 (predEnv s5) (predEnv s5)" using substate_asym pred3_sub3_def by auto
                     ultimately show ?thesis by auto
                   qed
                   thus ?thesis by auto
                 next
                   assume 6: "\<not>(getVarBool (predEnv s5) ''out_1'' = False \<or>
                              getVarBool (predEnv s5) ''inp_1'' = True)"
                   have "pred3_sub3 s2 s5" using substate_eq_or_predEnv 3 pred3_sub3_def substate_asym substate_trans by blast
                   obtain s4 where 7:"pred3_sub2_prems st4 s2 s4 s5 \<and> pred3_sub3 s4 s5" using 4 5 6 pred3_sub1_def pred3_sub2_def substate_eq_or_predEnv by blast
                   have "pred3_sub2_prems st4 s2 s4 (predEnv s5) \<and> pred3_sub3 s4 (predEnv s5)"
                   proof
                     show "pred3_sub2_prems st4 s2 s4 (predEnv s5)" using predEnv_substate substate_trans 7 pred3_sub2_prems_def by meson
                   next
                     show "pred3_sub3 s4 (predEnv s5)"
                     proof(simp only: pred3_sub3_def;rule allI;rule impI)
                       fix s3
                       assume 8: "toEnvP s3 \<and>
                                  substate (predEnv s5) s3 \<and>
                                  substate s3 s4 \<and> s3\<noteq>s4"
                       then have "s3 = predEnv s5 \<or> substate s5 s3" using 3(1) 7 predEnv_substate_imp_eq_or_substate pred3_sub2_prems_def by blast
                       then show "getVarBool s3 ''out_1'' = True \<and> getVarBool s3 ''inp_1'' = False" using 6 7 8 pred3_sub3_def by blast
                     qed
                   qed
                   thus ?thesis by auto
                 qed
               qed
             qed
             thus ?thesis using 2 s5_def R3_sub2_def R3_sub3_prems_def R3_sub3_def substate_refl prems R3_sub2_prems_def including pred3_defs by 
           qed
         qed
       qed
     qed
   qed
(*56*)

definition inv3 where "inv3 s \<equiv> R3_full s \<and> extraInv s"

lemma 
assumes base_inv:"(inv3 st0)"
 and st1:"st1=(setVarBool st0 ''inp_1'' inp_1)"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Wait''"
 and st1_if:"(getVarBool st1 ''inp_1'')=True"
 and st2:"(st2=(setVarBool st1 ''out_1'' True))"
 and st3:"(st3=(setPstate st2 ''Dryer'' ''Work''))"
 and st4:"(st4=(toEnv st3))"
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
 and st_final:"st_final=st4"
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
<<<<<<< HEAD
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
=======
                          getVarBool s1 ''inp_1'' = False \<and> getVarBool s1 ''out_1'' = True" using 7 prems predEnv_substate substate_asym substate_trans
                by (smt (verit))
              have "toEnvP s2 \<and> toEnvP st0 \<and> substate s2 st0 \<and>  s2 \<noteq> st0" using o1 sub5 0 by auto
              then have "\<exists> s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 st0 \<and>s2\<noteq>s3" using substate_eq_or_predEnv substate_refl by auto
              then have "\<exists> s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 st0 \<and>s2=(predEnv s3)" using predEnv_substate by sorry
              then obtain s3 where o2: "toEnvP s3 \<and> substate s3 st0 \<and> s2 = predEnv s3 " by auto
              then have "getPstate s3 ''Dryer'' = ''Wait''" using o1
                by (metis predEnv_substate predEnv_toEnvNum toEnvNum_id zero_neq_one)
              then have "getVarBool s3 ''out_1'' = False" using o2 extra1 extraInv_def by auto
              then have "toEnvP s3 \<and> substate x s3 \<and>substate s3 st_final \<and>
                        (toEnvNum x s3 \<le> 20 \<and>
                         (getVarBool s3 ''out_1'' = False \<or>
                          getVarBool s3 ''inp_1'' = True))" using o1 prems o2 substate_trans toEnvNum3 by sledgehammer
              then show ?thesis sorry
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
            qed
          next
            case bb
            then have "getPstate (predEnv x) ''Dryer'' = ''Wait''" using prems 3 o1 by blast
<<<<<<< HEAD
            then show ?thesis using 4 by auto  
          qed
        qed
      qed
    qed
    ultimately show ?thesis using P8_lemma P8_to_R3 by auto
  qed
qed
(*50*)
=======
            then show ?thesis using 4 by auto 
          qed
        qed
    

>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480

lemma 
assumes base_inv:"(inv4 st0)"
 and st1:"st1=(setVarBool st0 ''inp_1'' inp_1)"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Wait''"
 and st1_if:"(getVarBool st1 ''inp_1'')=True"
 and st2:"(st2=(setVarBool st1 ''out_1'' True))"
 and st3:"(st3=(setPstate st2 ''Dryer'' ''Work''))"
 and st4:"(st4=(toEnv st3))"
<<<<<<< HEAD
 and st_final:"st_final = st4"
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


=======
shows "(inv4 st4)" 
proof (simp add: inv4_def; intro conjI)
  show "extraInv st4" using assms extra inv4_def by simp
next
  show "R4 st4"
  proof (simp add: R4_def;intro conjI)
    show "toEnvP st4" using assms by simp
  next
    show "R4_sub1 st4"
    proof (simp only:R4_sub1_def;intro allI; rule impI)
      fix s1 s2
      assume prems: 
        "R4_sub2_prems st4 s1 s2"
      thus "getVarBool s2 ''out_1'' = True"
      proof (cases)
        assume 1:"s2=st4"
        thus ?thesis using assms by auto
      next
        assume 1: "s2\<noteq>st4"
        then have 2: "substate s2 st0" using prems assms R4_sub2_prems_def by (simp split: if_splits)
        thus ?thesis using R4_def R4_sub1_def R4_sub2_prems_def base_inv inv4_def prems by metis
      qed
    qed
  qed
qed

>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
end
