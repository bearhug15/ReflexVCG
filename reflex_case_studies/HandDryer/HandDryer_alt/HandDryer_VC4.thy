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
<<<<<<< HEAD
shows "(extraInv st4)" by sorry
 (* using assms extraInv_def apply auto
  done*)
=======
shows "(extraInv st4)"
  using assms extraInv_def apply auto
  done
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480

lemma
 assumes base_inv:"(inv1 st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Work''"
 and st1_if:"(getVarBool st1 ''inp_1'')=False"
 and st1_Work_timeout:"2000\<le>(ltime st1 ''Dryer'')"
 and st2:"(st2=(setVarBool st1 ''out_1'' False))"
 and st3:"(st3=(setPstate st2 ''Dryer'' ''Wait''))"
 and st4:"(st4=(toEnv st3))"
<<<<<<< HEAD
 and st_final:"st_final = st4"
shows "(inv1 st_final)"
proof (simp add: inv1_def; intro conjI)
  show "extraInv st_final" using assms extra inv1_def by simp
next
  show "R1_full st_final"
  proof -
    have 0:"toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms inv1_def extraInv_def by auto
    moreover have "P8_cons R1_A R1_B R1_C st0" using base_inv inv1_def R1_to_P8 by auto
    moreover have "( R1_A st_final st_final st0 \<longrightarrow> ( R1_B st_final st0 st_final \<and>  R1_C st_final st_final st_final))"
      apply (simp only: R1_A_def R1_B_def R1_C_def  SMT.verit_bool_simplify(4)) 
      using 0 substate_refl substate_asym toEnvNum_id assms by (smt (verit) extraInv_def getPstate.simps(3) inv1_def)
    moreover have "(\<forall>x. toEnvP x \<and> substate x st0 \<and> toEnvP (predEnv x) \<and> \<not>R1_A st0 x (predEnv x) \<and> R1_A st_final x (predEnv x)\<longrightarrow>
                    (\<exists> s4. toEnvP s4 \<and> substate x s4  \<and> substate s4 st_final \<and> R1_B x (predEnv x) s4 \<and>
                     (\<forall>s3. toEnvP s3 \<and> substate x s3 \<and> substate s3 s4 \<longrightarrow> R1_C x s4 s3)))"
      apply(auto simp only: R1_A_def)
      done
    ultimately show ?thesis using P8_lemma P8_to_R1 by blast
=======
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
        then obtain "R1_sub2 st0 s2" using assms prems inv1_def R1_def R1_sub1_def R1_sub2_prems_def by meson
        then obtain s4 where "R1_sub3_prems st0 s2 s4 \<and> R1_sub3 s2 s4" using R1_sub2_def by auto
        thus ?thesis using assms R1_sub2_def R1_sub3_prems_def substate.simps by metis
      qed
    qed
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
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
<<<<<<< HEAD
 and st_final: "st_final=st4"
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
=======
shows "(inv2 st4)"
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
<<<<<<< HEAD
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
      using assms R3_A_def R3_B_def R3_C_def by (metis bot_nat_0.extremum_unique toEnvNum_id zero_neq_numeral)
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
      then have prems:
          "toEnvP x \<and>
          substate x st0 \<and>
          toEnvP (predEnv x) \<and>
          getVarBool (predEnv x) ''inp_1'' = True \<and>
          getVarBool (predEnv x) ''out_1'' = True \<and>
          getVarBool x ''inp_1'' = False \<and>
          toEnvNum x st0 = 19 \<and>
          toEnvNum x st_final = 20" using 0 predEnv_toEnvNum predEnv_substate toEnvNum3 by force
      show "\<exists>s4. toEnvP s4 \<and> substate x s4 \<and>substate s4 st_final \<and>
              (toEnvNum x s4 \<le> 20 \<and>
               (getVarBool s4 ''out_1'' = False \<or>
                getVarBool s4 ''inp_1'' = True)) \<and>
              (\<forall>s3. (toEnvP s3 \<and> substate x s3 \<and> substate s3 s4) \<and> s3 \<noteq> s4 \<longrightarrow>
                    getVarBool s3 ''out_1'' = True \<and>
                    getVarBool s3 ''inp_1'' = False)"
      proof -
        have 1:"getPstate st0 ''Dryer'' = ''Work''" using assms by auto
        have 3:"substate (predEnv x) st0" using prems predEnv_substate substate_trans by blast
        have "toEnvP (predEnv x) \<and> toEnvP x \<and> 
                toEnvNum (predEnv x) x = 1 \<and> substate x st0 \<and>
                getPstate (predEnv x) ''Dryer'' = ''Work'' \<and> getVarBool (predEnv x) ''inp_1'' = True" 
            using prems predEnv_toEnvNum 3 predEnv_substate extra1 extraInv_def by auto
        then have 6:"getPstate x ''Dryer'' = ''Work'' \<and> ltime x ''Dryer'' div 100 = 1" using extra1 extraInv_def extra4_def by force
        consider (a) "(toEnvNum x st0+1) > ltime st0 ''Dryer'' div 100"
          | (b)"(toEnvNum x st0+1) = ltime st0 ''Dryer'' div 100"
          | (c)"(toEnvNum x st0+1) < ltime st0 ''Dryer'' div 100" by arith
        thus ?thesis 
        proof (cases)
          case a
          have 8:"\<forall>s1. toEnvP s1 \<and> substate x s1 \<and> substate s1 st0 \<longrightarrow> getPstate s1 ''Dryer'' = ''Work''"
          proof (rule allI;rule impI)
            fix s1
            assume prems2:"toEnvP s1 \<and> substate x s1 \<and> substate s1 st0"
            then consider (prems3)"getPstate s1 ''Dryer'' = ''Wait''" | (prems4)"getPstate s1 ''Dryer'' = ''Work''" using extra1 extraInv_def by auto
            thus "getPstate s1 ''Dryer'' = ''Work''"
            proof(cases)
              case prems3
              then have "((\<forall> s2. toEnvP s2 \<and> substate s2 s1 \<longrightarrow> 
                            getPstate s2 ''Dryer'' = ''Wait'') \<or>
                        (\<exists> s2. toEnvP s2 \<and> substate s2 s1 \<and>
                          getPstate s2 ''Dryer'' = ''Work'' \<and> ltime s2 ''Dryer'' div 100 \<ge>20 \<and>
                          (\<forall> s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s1 \<and>
                            s3\<noteq>s2 \<longrightarrow> getPstate s3 ''Dryer'' = ''Wait'')))" using extra1 extraInv_def extra2_def prems2 by auto
              then consider (a1)"(\<forall> s2. toEnvP s2 \<and> substate s2 s1 \<longrightarrow> 
                            getPstate s2 ''Dryer'' = ''Wait'') " |
                            (a2)"(\<exists> s2. toEnvP s2 \<and> substate s2 s1 \<and>
                            getPstate s2 ''Dryer'' = ''Work'' \<and> ltime s2 ''Dryer'' div 100 \<ge>20 \<and>
                            (\<forall> s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s1 \<and>
                              s3\<noteq>s2 \<longrightarrow> getPstate s3 ''Dryer'' = ''Wait''))" using extra1 extraInv_def extra2_def prems2 by auto
              thus ?thesis
              proof (cases)
                case a1
                then show ?thesis using prems2 6 prems by simp
              next
                case a2
                then obtain s2 where o2:
                  "toEnvP s2 \<and>
                   substate s2 s1 \<and>
                   getPstate s2 ''Dryer'' = ''Work'' \<and>
                   20 \<le> ltime s2 ''Dryer'' div 100 \<and>
                   (\<forall>s3. toEnvP s3 \<and>
                         substate s2 s3 \<and>
                         substate s3 s1 \<and> s3 \<noteq> s2 \<longrightarrow>
                         getPstate s3 ''Dryer'' = ''Wait'')" by auto
                have "s1\<noteq>st0" using "1" prems3 by force
                then have 8:"toEnvNum s1 st0 \<ge>1" using substate_eq_or_predEnv prems2 predEnv_toEnvNum prems3 1 substate_toEnvNum_id 
                  by (meson "0" leI less_one)
                have 9:"toEnvNum s2 s1 \<ge>1" using substate_eq_or_predEnv o2 prems2 predEnv_toEnvNum substate_toEnvNum_id prems3 by force
                then have 10:"toEnvNum s2 st0 \<ge>2" using toEnvNum3 substate_eq_or_predEnv 8 o2 prems2 by auto
                consider (a21)"substate s2 x \<and> s2\<noteq>x" | (a22)"substate x s2" using o2 prems2 substate_total by auto
                thus ?thesis
                proof (cases)
                  case a21
                  then show ?thesis using 6 o2 prems prems2 by force
                next
                  case a22
                  then have 11:"substate x s2 \<and> substate s2 st0" using o2 prems2 substate_trans by blast
                  then have "toEnvNum x s2 \<le> 18" using 10 prems toEnvNum3 by force 
                  then have "ltime s2 ''Dryer'' \<le> 19" using 6 ltime_after_toEnvNum o2 prems 11 
                    by (smt (verit, best) \<open>toEnvP x \<and> substate x st0 \<and> toEnvP (predEnv x) \<and> \<not> (20 \<le> toEnvNum x st0 \<and> getVarBool (predEnv x) ''inp_1'' = True \<and> getVarBool (predEnv x) ''out_1'' = True \<and> getVarBool x ''inp_1'' = False) \<and> 20 \<le> toEnvNum x st_final \<and> getVarBool (predEnv x) ''inp_1'' = True \<and> getVarBool (predEnv x) ''out_1'' = True \<and> getVarBool x ''inp_1'' = False\<close> add_le_cancel_left le_trans numeral_Bit0 numeral_Bit1 one_plus_numeral_commute)
                  then show ?thesis using o2 by auto
                qed
              qed
            next
              case prems4
              thus ?thesis by auto
            qed
          qed
          have "toEnvP st0 \<and>
                toEnvP x \<and>
                substate x st0 \<and>
                substate st0 st0 \<and>
                getPstate st0 ''Dryer'' = ''Work'' \<and>
                getPstate x ''Dryer'' = ''Work'' \<and>
                ltime st0 ''Dryer'' div 100 < toEnvNum x st0 + 1" using 0 substate_refl 1 6 prems a by auto
          then consider 
            (a1)"(\<exists>s3. toEnvP s3 \<and>
              substate x s3 \<and>substate s3 st0 \<and>
              getVarBool s3 ''inp_1'' = True \<and>
              (\<forall>s4. toEnvP s4 \<and>
                    substate x s4 \<and> substate s4 s3 \<and> s4 \<noteq> s3 \<longrightarrow>
                    getVarBool s3 ''inp_1'' = False \<and>
                    getVarBool s3 ''out_1'' = True))"
            | (a2)"(\<exists>s3 s4. toEnvP s3 \<and> toEnvP s4 \<and>
            substate x s3 \<and> substate s3 s4 \<and> substate s4 st0 \<and>
            toEnvNum s3 s4 = 1 \<and> getPstate s3 ''Dryer'' = ''Work'' \<and>
            getPstate s4 ''Dryer'' = ''Wait'')" using extra1 extraInv_def extra5_def by blast
          thus ?thesis 
          proof (cases)
            case a1
            then obtain s3 where o1:
              "toEnvP s3 \<and>
               substate x s3 \<and>
               substate s3 st0 \<and>
               getVarBool s3 ''inp_1'' = True \<and>
               (\<forall>s4. toEnvP s4 \<and> substate x s4 \<and> substate s4 s3 \<and> s4 \<noteq> s3 \<longrightarrow>
                     getVarBool s3 ''inp_1'' = False \<and>
                     getVarBool s3 ''out_1'' = True)" by auto
            then show ?thesis using prems substate_total by blast
          next
            case a2
            then obtain s3 s4 where o1:
              "toEnvP s3 \<and>
               toEnvP s4 \<and>
               substate x s3 \<and>
               substate s3 s4 \<and>
               substate s4 st0 \<and>
               toEnvNum s3 s4 = 1 \<and>
               getPstate s3 ''Dryer'' = ''Work'' \<and> 
               getPstate s4 ''Dryer'' = ''Wait''" by auto
            then have "getPstate s4 ''Dryer'' = ''Work''" using 8 substate_trans by blast
            then show ?thesis using o1 by auto 
          qed
        next
          case b
          then show ?thesis using prems extra1 extraInv_def extra1_def 0 1 substate_refl by force
        next
          case c
          then have "getPstate st0 ''Dryer'' = getPstate x ''Dryer''" using toEnvNum_getPstate 
            by (metis add_lessD1)
          have 5:"(\<forall> s1. toEnvP s1 \<and> substate s1 st0 \<and>
                 (toEnvNum s1 st0+1) < ltime st0 ''Dryer'' div 100\<longrightarrow>
                    getVarBool s1 ''inp_1'' = False \<and>
                    getVarBool s1 ''out_1'' = True)" using extra1 extraInv_def extra1_def substate_refl 0 1 by blast
          have "toEnvP st_final \<and> substate x st_final \<and>substate st_final st_final \<and> (toEnvNum x st_final \<le> 20 \<and>
                       (getVarBool st_final ''out_1'' = False \<or>
                        getVarBool st_final ''inp_1'' = True))" using prems assms by auto
          moreover have "(\<forall>s3. (toEnvP s3 \<and> substate x s3 \<and> substate s3 st_final) \<and> s3\<noteq>st_final\<longrightarrow>
                    getVarBool s3 ''out_1'' = True \<and>
                    getVarBool s3 ''inp_1'' = False)"
          proof(rule allI;rule impI)
            fix s3
            assume "(toEnvP s3 \<and> substate x s3 \<and> substate s3 st_final) \<and> s3\<noteq>st_final"
            then have "(toEnvP s3 \<and> substate x s3 \<and> substate s3 st0)" using 0 substate_trans predEnv_substate substate_eq_or_predEnv by blast
            moreover have "(toEnvNum s3 st0+1) < ltime st0 ''Dryer'' div 100" using inter_toEnvNum_getPstate 
              by (smt (verit, best) "0" add_diff_cancel_right' add_le_add_imp_diff_le c calculation le_trans less_or_eq_imp_le prems shift_toEnvNum substate_asym substate_shift verit_comp_simplify1(3))
            ultimately show  "getVarBool s3 ''out_1'' = True \<and> getVarBool s3 ''inp_1'' = False" using 5 by blast
          qed
          ultimately show ?thesis by auto
        qed
      qed
    qed
    ultimately show ?thesis using P8_lemma P8_to_R3 by auto
  qed
qed
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
             thus ?thesis using 2 s5_def R3_sub2_def R3_sub3_prems_def R3_sub3_def substate_refl prems R3_sub2_prems_def including pred3_defs by auto
           qed
         qed
       qed
     qed
   qed
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480

lemma
 assumes base_inv:"(inv4 st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Work''"
 and st1_if:"(getVarBool st1 ''inp_1'')=False"
 and st1_Work_timeout:"2000\<le>(ltime st1 ''Dryer'')"
 and st2:"(st2=(setVarBool st1 ''out_1'' False))"
 and st3:"(st3=(setPstate st2 ''Dryer'' ''Wait''))"
 and st4:"(st4=(toEnv st3))"
<<<<<<< HEAD
 and st_final:"st_final=st4"
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
        thus ?thesis using assms prems by auto
      next
        assume 1: "s2\<noteq>st4"
        then have 2: "substate s2 st0" using prems assms R4_sub2_prems_def by (simp split: if_splits)
        thus ?thesis using R4_def R4_sub1_def R4_sub2_prems_def base_inv inv4_def prems by metis
      qed
    qed
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
  qed
qed

end
