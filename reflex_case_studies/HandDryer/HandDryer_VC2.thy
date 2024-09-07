theory HandDryer_VC2
imports HandDryerTheory
begin

lemma extra:
 assumes base_inv:"(extraInv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Wait''"
 and st1_if:"(getVarBool st1 ''inp_1'')=False"
 and st2:"(st2=(toEnv st1))"
shows "(extraInv st2)"
  using assms extraInv_def apply auto
  done

lemma
 assumes base_inv:"(inv1 st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Wait''"
 and st1_if:"(getVarBool st1 ''inp_1'')=False"
 and st2:"(st2=(toEnv st1))"
shows "(inv1 st2)"
proof (simp add: inv1_def; intro conjI)
  show "extraInv st2" using assms extra inv1_def by simp
next
  show "R1 st2"
  proof (simp add: R1_def;intro conjI)
    show "toEnvP st2" using assms by simp
  next
    show "R1_sub1 st2" 
    proof (simp add:R1_sub1_def;rule allI;rule allI; rule impI)
      fix s1 s2
      assume prems: 
        "R1_sub2_prems st2 s1 s2"
<<<<<<< HEAD
      consider (a) "s2=st2" | (b) "s2\<noteq>st2" using R1_sub2_prems_def by auto
      thus "R1_sub2 st2 s2"
      proof (cases)
        case a
        have "R1_sub3_prems st2 s2 s2 \<and> R1_sub3 s2 s2" 
        proof
          from a show "R1_sub3_prems st2 s2 s2" using prems R1_sub3_prems_def assms R1_sub2_prems_def by auto
=======
      thus "R1_sub2 st2 s2"
      proof (simp only:R1_sub2_prems_def; cases)
        assume 1:"s2=st2"
        have "R1_sub3_prems st2 s2 s2 \<and> R1_sub3 s2 s2" 
        proof
          from 1 show "R1_sub3_prems st2 s2 s2" using prems R1_sub3_prems_def assms R1_sub2_prems_def by auto
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
        next show "R1_sub3 s2 s2" using R1_sub3_def R1_sub4_prems_def substate_asym by auto
        qed
        thus ?thesis using R1_sub2_def by blast
      next
<<<<<<< HEAD
        case b
=======
        assume 1: "s2\<noteq>st2"
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
        then have 2: "substate s2 st0" using prems assms R1_sub2_prems_def by (simp split: if_splits)
        then obtain "R1_sub2 st0 s2" using assms prems inv1_def R1_def R1_sub1_def R1_sub2_prems_def by auto
        then obtain s4 where 3:"R1_sub3_prems st0 s2 s4 \<and> R1_sub3 s2 s4" including R1_defs  by auto
        thus ?thesis using 3 assms substate.simps R1_sub2_def R1_sub3_prems_def by meson
      qed
    qed
  qed
qed

lemma
 assumes base_inv:"(inv2 st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Wait''"
 and st1_if:"(getVarBool st1 ''inp_1'')=False"
 and st2:"(st2=(toEnv st1))"
shows "(inv2 st2)"
proof (simp add: inv2_def; intro conjI)
show "extraInv st2" using assms extra inv2_def by simp
next
  show "R2 st2"
  proof (simp add: R2_def;intro conjI)
    show "toEnvP st2" using assms by simp
  next
    show "R2_sub1 st2"
    proof (simp only:R2_sub1_def; rule allI;rule allI;rule impI)
      fix s1 s2
      assume prems:"R2_sub2_prems st2 s1 s2"
<<<<<<< HEAD
      consider (a) "s2=st2" | (b) "s2\<noteq>st2" using R2_sub2_prems_def by auto
      thus "getVarBool s2 ''out_1'' = False"
      proof (cases)
        case a
        thus ?thesis using assms prems R2_sub2_prems_def inv2_def R2_def 
          by (smt (z3) add_cancel_right_left getVarBool.simps(2) getVarBool.simps(3) substate.simps(2) substate.simps(3) substate_toEnvNum_id toEnvNum.simps(2) toEnvNum.simps(3)) 
      next
        case b
        thus ?thesis using assms prems R2_def R2_sub1_def R2_sub2_prems_def inv2_def 
          by (metis state.distinct(15) substate.simps(2) substate.simps(3) toEnvP.elims(2))
=======
      thus "getVarBool s2 ''out_1'' = False"
      proof (simp only: R2_sub2_prems_def; cases)
        assume 1: "s2=st2"
        thus ?thesis using assms prems R2_sub2_prems_def inv2_def R2_def 
        by (smt (z3) add_cancel_right_left getVarBool.simps(2) getVarBool.simps(3) substate.simps(2) substate.simps(3) substate_toEnvNum_id toEnvNum.simps(2) toEnvNum.simps(3)) 
      next
        assume 2: "s2\<noteq>st2"
        thus ?thesis using assms prems R2_def R2_sub1_def R2_sub2_prems_def inv2_def 
        by (metis state.distinct(15) substate.simps(2) substate.simps(3) toEnvP.elims(2))
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
      qed
    qed
  qed
qed

lemma
 assumes base_inv:"(inv3 st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Wait''"
 and st1_if:"(getVarBool st1 ''inp_1'')=False"
 and st2:"(st2=(toEnv st1))"
shows "(inv3 st2)"
proof (simp add: inv3_def; intro conjI)
  show "extraInv st2" using assms extra inv3_def by simp
next
<<<<<<< HEAD
  have extra:"extraInv st2" using assms extra inv3_def by simp
  show "R3 st2"
  proof (simp add: R3_def;intro conjI)
    show "toEnvP st2" using assms by simp
  next
    show "R3_sub1 st2"
    proof(simp only: R3_sub1_def; intro allI; rule impI)
      fix s1 s2
      assume prems:"R3_sub2_prems st2 s1 s2" 
      then obtain "20 \<le> toEnvNum s2 st2" using R3_sub2_prems_def by auto
      from le_imp_less_or_eq[OF this] show "R3_sub2 st2 s2"
        apply (rule disjE)
      proof -
        assume 1:"20 < toEnvNum s2 st2"
        have 2: "substate s2 st0" using assms prems R3_sub2_prems_def substate_eq_or_predEnv toEnvNum_id by (simp split:if_splits)
        obtain "R3_sub1 st0" using prems R3_def base_inv inv3_def by auto
        then have "R3_sub2 st0 s2" using 1 2 prems assms R3_sub2_def R3_sub2_prems_def R3_sub1_def by (auto simp add: split:if_splits)
        then obtain s4 where 3: "R3_sub3_prems st0 s2 s4 \<and> R3_sub3 s2 s4" using R3_sub2_def by auto
        then have "R3_sub3_prems st2 s2 s4 \<and> R3_sub3 s2 s4" using assms R3_sub3_prems_def by auto
        thus ?thesis using R3_sub2_def by auto
      next
        assume 1: "20 = toEnvNum s2 st2"
        have 2:"R3_sub3 s2 s2" using substate_asym R3_sub3_def R3_sub4_prems_def by auto
        show ?thesis
        proof -
          define s5::state where "s5=s2"
          have "toEnvP s5 \<and> substate s2 s5 \<and> substate s5 st2
                \<longrightarrow> pred3 st2 s2 s5" including pred3_defs
             proof(induction rule: state_down_ind)
               case 1
               then show ?case using prems assms R3_sub2_prems_def by auto
             next
               case 2
               then show ?case
                 apply(simp only: pred3_def)
               proof
                 assume 3:"pred3_sub1 s2 st2"
                 have "pred3_sub2_prems st2 s2 st2 st2 \<and> pred3_sub3 st2 st2" 
                 proof(rule conjI)
                   have "getPstate st2 ''Dryer'' = ''Wait''" using assms by auto
                   then have "getVarBool st2 ''out_1'' = False" using extraInv_def extra by blast
                   thus "pred3_sub2_prems st2 s2 st2 st2" 
                     using 1 assms pred3_sub2_prems_def prems substate_refl inv4_def extraInv_def by auto
                 next
                   show "pred3_sub3 st2 st2" using substate_asym pred3_sub3_def by blast
                 qed
                 thus "pred3_sub2 st2 s2 st2" using pred3_sub2_def by auto
               qed
             next
               case (3 s5)
               then show ?case 
                 apply (simp only: pred3_def)
               proof
                 from 3(3) 
                 have 4:"pred3_sub1 s2 s5 \<Longrightarrow> pred3_sub2 st2 s2 s5" using pred3_def by blast
                 assume 5: "pred3_sub1 s2 (predEnv s5)"
                 show "pred3_sub2 st2 s2 (predEnv s5)"
                 proof cases
                   assume 6: "getVarBool (predEnv s5) ''out_1'' = False \<or>
                              getVarBool (predEnv s5) ''inp_1'' = True"
                   have 7: "substate (predEnv s5) st2" using predEnv_substate substate_trans 3 by blast
                   have 8: "substate s2 (predEnv s5)" using 3(2) substate_eq_or_predEnv prems R3_sub2_prems_def by auto
                   have "pred3_sub2_prems st2 s2 (predEnv s5) (predEnv s5) \<and> pred3_sub3 (predEnv s5) (predEnv s5)"
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
                     moreover have "substate (predEnv s5) st2" using 7 by assumption
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
                   obtain s4 where 7:"pred3_sub2_prems st2 s2 s4 s5 \<and> pred3_sub3 s4 s5" using 4 5 6 pred3_sub1_def pred3_sub2_def substate_eq_or_predEnv by blast
                   have "pred3_sub2_prems st2 s2 s4 (predEnv s5) \<and> pred3_sub3 s4 (predEnv s5)"
                   proof
                     show "pred3_sub2_prems st2 s2 s4 (predEnv s5)" using predEnv_substate substate_trans 7 pred3_sub2_prems_def by meson
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
             thus ?thesis using 2 s5_def R3_sub2_def R3_sub3_prems_def R3_sub3_def substate_refl prems R3_sub2_prems_def including pred3_defs by sledgehammer
           qed
         qed
       qed
     qed
   qed


(*
proof (simp add: inv3_def; intro conjI)
  show "extraInv st2" using assms extra inv3_def by simp
next
=======
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
  have ex:"extraInv st2" using assms extra inv3_def by simp
  show "R3 st2"
  proof (simp add: R3_def;intro conjI)
    show "toEnvP st2" using assms by simp
  next
    show "R3_sub1 st2"
    proof(simp only: R3_sub1_def; intro allI; rule impI)
      fix s1 s2
      assume prems:"R3_sub2_prems st2 s1 s2" 
      then obtain "20 \<le> toEnvNum s2 st2" using R3_sub2_prems_def by auto
      from le_imp_less_or_eq[OF this] show "R3_sub2 st2 s2"
        apply (rule disjE)
      proof -
        assume 1:"20 < toEnvNum s2 st2"
        have 2: "substate s2 st0" using assms prems R3_sub2_prems_def substate_eq_or_predEnv toEnvNum_id by (simp split:if_splits)
        obtain "R3_sub1 st0" using prems R3_def base_inv inv3_def by auto
        then have "R3_sub2 st0 s2" using 1 2 prems assms R3_sub2_def R3_sub2_prems_def R3_sub1_def by (auto simp add: split:if_splits)
        then obtain s4 where 3: "R3_sub3_prems st0 s2 s4 \<and> R3_sub3 s2 s4" using R3_sub2_def by auto
        then have "R3_sub3_prems st2 s2 s4 \<and> R3_sub3 s2 s4" using assms R3_sub3_prems_def by auto
        thus ?thesis using R3_sub2_def by auto
      next
        assume 1: "20 = toEnvNum s2 st2"
        have 2:"R3_sub3 s2 s2" using substate_asym R3_sub3_def R3_sub4_prems_def by auto
        show ?thesis
        proof -
          define s5::state where "s5=s2"
          have "toEnvP s5 \<and> substate s2 s5 \<and> substate s5 st2
                \<longrightarrow> pred3 st2 s2 s5" including pred3_defs
             proof(induction rule: state_down_ind)
               case 1
               then show ?case using prems assms R3_sub2_prems_def by auto
             next
               case 2
               then show ?case
                 apply(simp only: pred3_def)
               proof
                 assume 3:"pred3_sub1 s2 st2"
                 have "pred3_sub2_prems st2 s2 st2 st2 \<and> pred3_sub3 st2 st2" 
                 proof(rule conjI)
                   have "toEnvP st2 \<and>
                         substate st2 st2 \<and>
                         substate st2 st2 \<and>
                         toEnvNum s2 st2\<le> 20" using 1 assms substate_refl by auto
                   moreover have "(getVarBool st2 ''out_1'' = False \<or>
                                   getVarBool st2 ''inp_1'' = True)" using ex extraInv_def assms by auto
                   ultimately show "pred3_sub2_prems st2 s2 st2 st2" by auto
                 next
                   show "pred3_sub3 st2 st2" using substate_asym pred3_sub3_def by blast
                 qed
                 thus "pred3_sub2 st2 s2 st2" using pred3_sub2_def by auto
               qed
             next
               case (3 s5)
               then show ?case 
                 apply (simp only: pred3_def)
               proof
                 from 3(3) 
                 have 4:"pred3_sub1 s2 s5 \<Longrightarrow> pred3_sub2 st2 s2 s5" using pred3_def by blast
                 assume 5: "pred3_sub1 s2 (predEnv s5)"
                 show "pred3_sub2 st2 s2 (predEnv s5)"
                 proof cases
                   assume 6: "getVarBool (predEnv s5) ''out_1'' = False \<or>
                              getVarBool (predEnv s5) ''inp_1'' = True"
                   have 7: "substate (predEnv s5) st2" using predEnv_substate substate_trans 3 by blast
                   have 8: "substate s2 (predEnv s5)" using 3(2) substate_eq_or_predEnv prems R3_sub2_prems_def by auto
                   have "pred3_sub2_prems st2 s2 (predEnv s5) (predEnv s5) \<and> pred3_sub3 (predEnv s5) (predEnv s5)"
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
                     moreover have "substate (predEnv s5) st2" using 7 by assumption
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
                   obtain s4 where 7:"pred3_sub2_prems st2 s2 s4 s5 \<and> pred3_sub3 s4 s5" using 4 5 6 pred3_sub1_def pred3_sub2_def substate_eq_or_predEnv by blast
                   have "pred3_sub2_prems st2 s2 s4 (predEnv s5) \<and> pred3_sub3 s4 (predEnv s5)"
                   proof
                     show "pred3_sub2_prems st2 s2 s4 (predEnv s5)" using predEnv_substate substate_trans 7 pred3_sub2_prems_def by meson
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
<<<<<<< HEAD
             thus ?thesis using 2 s5_def R3_sub2_def R3_sub3_prems_def R3_sub3_def substate_refl prems R3_sub2_prems_def including pred3_defs by sledgehammer
=======
             thus ?thesis using 2 s5_def R3_sub2_def R3_sub3_prems_def R3_sub3_def substate_refl prems R3_sub2_prems_def including pred3_defs by auto
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
           qed
         qed
       qed
     qed
   qed
<<<<<<< HEAD
*)
=======

>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
lemma
 assumes base_inv:"(inv4 st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Wait''"
 and st1_if:"(getVarBool st1 ''inp_1'')=False"
 and st2:"(st2=(toEnv st1))"
shows "(inv4 st2)"
proof (simp add: inv4_def; intro conjI)
  show "extraInv st2" using assms extra inv4_def by simp
next
  show "R4 st2"
  proof (simp add: R4_def;intro conjI)
    show "toEnvP st2" using assms by simp
  next
    show "R4_sub1 st2"
    proof (simp only:R4_sub1_def;intro allI; rule impI)
      fix s1 s2
<<<<<<< HEAD
      assume prems: "R4_sub2_prems st2 s1 s2"
      consider (a) "s2=st2" | (b) "s2\<noteq>st2" using R4_sub2_prems_def by auto
      thus "getVarBool s2 ''out_1'' = True"
      proof (cases)
        case a
        thus ?thesis using assms prems R4_sub2_prems_def by auto
      next
        case b
=======
      assume prems: 
        "R4_sub2_prems st2 s1 s2"
      thus "getVarBool s2 ''out_1'' = True"
      proof (cases)
        assume 1:"s2=st2"
        thus ?thesis using assms prems R4_sub2_prems_def by auto
      next
        assume 1: "s2\<noteq>st2"
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
        then have 2: "substate s2 st0" using prems assms R4_sub2_prems_def by (simp split: if_splits)
        thus ?thesis using R4_def R4_sub1_def R4_sub2_prems_def base_inv inv4_def prems by metis
      qed
    qed
  qed
qed

end
