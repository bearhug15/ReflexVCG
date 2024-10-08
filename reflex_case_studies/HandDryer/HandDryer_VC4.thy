theory HandDryer_VC4
imports HandDryerTheory
begin

lemma extra:
 assumes base_inv:"(extraInv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Work''"
 and st1_if:"(getVarBool st1 ''inp_1'')=False"
 and st1_Work_timeout:"2000\<le>(ltime st1 ''Dryer'')"
 and st2:"(st2=(setVarBool st1 ''out_1'' False))"
 and st3:"(st3=(setPstate st2 ''Dryer'' ''Wait''))"
 and st4:"(st4=(toEnv st3))"
shows "(extraInv st4)"
  using assms extraInv_def apply auto
  done

lemma
 assumes base_inv:"(inv1 st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Work''"
 and st1_if:"(getVarBool st1 ''inp_1'')=False"
 and st1_Work_timeout:"2000\<le>(ltime st1 ''Dryer'')"
 and st2:"(st2=(setVarBool st1 ''out_1'' False))"
 and st3:"(st3=(setPstate st2 ''Dryer'' ''Wait''))"
 and st4:"(st4=(toEnv st3))"
shows "(inv1 st4)"
proof (simp only: inv1_def; rule conjI)
  show "extraInv st4" using assms inv1_def extra by auto
next
  show "R1 st4"
  proof (simp add: R1_def;rule conjI)
    show "toEnvP st4" using assms by simp
  next
    show "R1_sub1 st4"
    proof (simp add:R1_sub1_def; intro allI;rule impI)
      fix s1 s2
      assume prems:"R1_sub2_prems st4 s1 s2"
<<<<<<< HEAD
      consider (a) "s2=st4" | (b) "s2\<noteq>st4" using R1_sub2_prems_def by auto
      thus "R1_sub2 st4 s2"
      proof (cases)
        case a
        have "R1_sub3_prems st4 s2 s2 \<and> R1_sub3 s2 s2"
        proof
          show "R1_sub3_prems st4 s2 s2" using a assms R1_sub3_prems_def R1_sub2_prems_def prems by auto
        next
          show "R1_sub3 s2 s2" using a assms R1_sub3_def R1_sub4_prems_def substate_asym by blast
        qed
        thus ?thesis using R1_sub2_def by blast
      next
        case b
        then have "substate s2 st0" using prems assms R1_sub2_prems_def substate.simps
          by (metis toEnvP.simps(3) toEnvP.simps(7))
=======
      thus "R1_sub2 st4 s2"
      proof (simp only: R1_sub2_prems_def; cases)
        assume 1:"s2=st4"
        have "R1_sub3_prems st4 s2 s2 \<and> R1_sub3 s2 s2"
        proof
          show "R1_sub3_prems st4 s2 s2" using 1 assms R1_sub3_prems_def R1_sub2_prems_def prems by auto
        next
          show "R1_sub3 s2 s2" using 1 assms R1_sub3_def R1_sub4_prems_def substate_asym by blast
        qed
        thus ?thesis using R1_sub2_def by blast
      next
        assume 2:"s2\<noteq>st4"
        then have "substate s2 st0" using prems assms R1_sub2_prems_def substate.simps
        by (metis toEnvP.simps(3) toEnvP.simps(7))
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
        then obtain "R1_sub2 st0 s2" using assms prems inv1_def R1_def R1_sub1_def R1_sub2_prems_def by meson
        then obtain s4 where "R1_sub3_prems st0 s2 s4 \<and> R1_sub3 s2 s4" using R1_sub2_def by auto
        thus ?thesis using assms R1_sub2_def R1_sub3_prems_def substate.simps by metis
      qed
    qed
  qed
qed

lemma
 assumes base_inv:"(inv2 st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Work''"
 and st1_if:"(getVarBool st1 ''inp_1'')=False"
 and st1_Work_timeout:"2000\<le>(ltime st1 ''Dryer'')"
 and st2:"(st2=(setVarBool st1 ''out_1'' False))"
 and st3:"(st3=(setPstate st2 ''Dryer'' ''Wait''))"
 and st4:"(st4=(toEnv st3))"
shows "(inv2 st4)"
<<<<<<< HEAD
proof (simp add: inv2_def; intro conjI)
  show "extraInv st4" using extra assms inv2_def by auto
next
  show "R2 st4"
  proof (simp add: R2_def;intro conjI)
    show "toEnvP st4" using assms by simp
  next
    show "R2_sub1 st4"
    proof (simp only:R2_sub1_def; rule allI;rule allI;rule impI)
      fix s1 s2
      assume prems:"R2_sub2_prems st4 s1 s2"
      consider (a) "s2=st4" | (b) "s2\<noteq>st4" using R2_sub2_prems_def by auto
      thus "getVarBool s2 ''out_1'' = False"
      proof (cases)
        case a
        thus ?thesis using assms prems R2_sub2_prems_def by auto
      next
        case b
        thus ?thesis using assms prems R2_def R2_sub1_def R2_sub2_prems_def inv2_def
          by (metis substate.simps(2) substate.simps(3) substate.simps(7) toEnvP.simps(3) toEnvP.simps(7))
      qed
    qed
  qed 
=======
proof -
  from assms show ?thesis
  apply (simp only: inv2_def assms R2_def R2_sub1_def R2_sub2_prems_def)
    apply (intro conjI)
      apply auto
    using extra assms by presburger
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
qed

lemma
 assumes base_inv:"(inv3 st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Work''"
 and st1_if:"(getVarBool st1 ''inp_1'')=False"
 and st1_Work_timeout:"2000\<le>(ltime st1 ''Dryer'')"
 and st2:"(st2=(setVarBool st1 ''out_1'' False))"
 and st3:"(st3=(setPstate st2 ''Dryer'' ''Wait''))"
 and st4:"(st4=(toEnv st3))"
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
             thus ?thesis using 2 s5_def R3_sub2_def R3_sub3_prems_def R3_sub3_def substate_refl prems R3_sub2_prems_def including pred3_defs by auto
           qed
         qed
       qed
     qed
   qed

lemma
 assumes base_inv:"(inv4 st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Work''"
 and st1_if:"(getVarBool st1 ''inp_1'')=False"
 and st1_Work_timeout:"2000\<le>(ltime st1 ''Dryer'')"
 and st2:"(st2=(setVarBool st1 ''out_1'' False))"
 and st3:"(st3=(setPstate st2 ''Dryer'' ''Wait''))"
 and st4:"(st4=(toEnv st3))"
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
<<<<<<< HEAD
      assume prems: "R4_sub2_prems st4 s1 s2"
      consider (a) "s2=st4" | (b) "s2\<noteq>st4" using R2_sub2_prems_def by auto
      thus "getVarBool s2 ''out_1'' = True"
      proof (cases)
        case a
        thus ?thesis using assms prems R4_sub2_prems_def by simp
      next
        case b
=======
      assume prems: 
        "R4_sub2_prems st4 s1 s2"
      thus "getVarBool s2 ''out_1'' = True"
      proof (cases)
        assume 1:"s2=st4"
        thus ?thesis using assms prems by auto
      next
        assume 1: "s2\<noteq>st4"
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
        then have 2: "substate s2 st0" using prems assms R4_sub2_prems_def by (simp split: if_splits)
        thus ?thesis using R4_def R4_sub1_def R4_sub2_prems_def base_inv inv4_def prems by metis
      qed
    qed
  qed
qed

end
