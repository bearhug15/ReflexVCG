theory ReflexPatterns
  imports Reflex
begin

(*
usefull lemmas:
SMT.verit_bool_simplify(4)
*)

find_theorems name:SMT.verit_bool_simplify
section "Pattern 1"

definition P1 where
"P1 A s = (\<forall>x. toEnvP x \<and> substate x s \<longrightarrow> A x)"

lemma P1_lemma:
  fixes A1 :: "state \<Rightarrow>bool"
  assumes "toEnvP s \<and> toEnvP s' \<and> s= predEnv s'"
  and "P1 A1 s"
  and "A1 s'"
shows "P1 A1 s'"
proof(simp add: P1_def; rule allI; rule impI)
  fix x::state
  assume prems:"toEnvP x \<and> substate x s'"
  show "A1 x"
  proof (cases)
    assume "x=s'"
    thus ?thesis using assms by auto
  next
    assume 1:"x\<noteq>s'"
    then have "substate x s \<or> (substate s x \<and> substate x s' \<and> x \<noteq> s \<and> x \<noteq> s')" using substate_trans substate_asym substate_eq_or_predEnv
    using assms(1) prems by blast
    then show ?thesis 
    by (metis P1_def assms(1) assms(2) prems substate_eq_or_predEnv)
  qed
qed

definition P1f where
"P1f A s \<equiv>(\<forall>s1. toEnvP s1 \<and> substate s1 s \<longrightarrow> A s s1)"

lemma P1f_lemma:
  assumes "toEnvP s \<and> toEnvP s' \<and> s= predEnv s'"
  and "P1f A1 s"
  and "A1 s' s'"
  and "(\<forall>x. toEnvP x \<and> substate x s \<and> A1 s x \<longrightarrow> A1 s' x)"
shows "P1f A1 s'" 
  using P1f_def assms substate_eq_or_predEnv by blast 


section "Pattern 2"

definition P2 where
"P2 A s = (\<forall>x y. toEnvP y \<and> toEnvP x \<and> substate x y \<and> substate y s \<longrightarrow> A y x)"

lemma P2_lemma:
  assumes "toEnvP s \<and> toEnvP s' \<and> s= predEnv s'"
  and "P2 A2 s"
  and "A1 = A2 s'"
  and "P1 A1 s'"
shows "P2 A2 s'"
proof(simp add: P2_def; intro allI; rule impI)
  fix x y::state
  assume prems:"toEnvP y \<and> toEnvP x \<and> substate x y \<and> substate y s'"
  show "A2 y x"
  proof (cases)
    assume "y=s'"
    thus ?thesis using assms using P1_def prems by blast
  next
    assume 1:"y\<noteq>s'"
    then have "substate y s \<or> (substate s y \<and> substate y s' \<and> y \<noteq> s \<and> y \<noteq> s')" using substate_trans substate_asym substate_eq_or_predEnv
    using assms(1) prems by blast
    then show ?thesis 
    by (metis P2_def assms(1) assms(2) prems substate_eq_or_predEnv)
  qed
qed

lemma P2_lemma_exp:
  fixes A2 :: "state\<Rightarrow>state\<Rightarrow>bool"
  assumes "toEnvP s \<and> toEnvP s' \<and> s= predEnv s'"
  and "P2 A2 s"
  and "A1 = A2 s'"
  and "P1 A1 s"
  and "A1 s'"
shows "P2 A2 s'"
  using assms P1_lemma P2_lemma by blast

section "Pattern 2.1"

definition P2_1_cons where
"P2_1_cons A s \<equiv> (\<forall>x y. toEnvP y \<and> toEnvP x \<and> substate x y \<and> substate y s \<and> toEnvNum x y =1 \<longrightarrow> A y x)"

definition cons_to_predEnv where
"cons_to_predEnv A = (\<lambda> s. A s (predEnv s))"

definition P2_1_predEnv where
"P2_1_predEnv A s \<equiv> (\<forall>x.  toEnvP x \<and> substate x s \<and> toEnvP (predEnv x) \<longrightarrow> A x (predEnv x))"

lemma P2_1_bridge:
"P2_1_predEnv A s = P2_1_cons A s"
proof 
  assume prem1:"P2_1_predEnv A s"
  thus "P2_1_cons A s"
  proof (simp only:P2_1_cons_def P2_1_predEnv_def; intro allI; intro impI)
    fix x y
    assume prem2:"toEnvP y \<and>
                   toEnvP x \<and>
                   substate x y \<and>
                   substate y s \<and>
                   toEnvNum x y = 1"
    then have "x = predEnv y" using prem2 toEnvNum_predEnv by auto
    thus "A y x" using prem1 P2_1_predEnv_def prem2  by auto
  qed
next
  assume prem1:"P2_1_cons A s"
  thus "P2_1_predEnv A s"
  proof (simp only:P2_1_cons_def P2_1_predEnv_def; intro allI; intro impI)
    fix x
    assume prem2:"toEnvP x \<and>
                   substate x s \<and>
                   toEnvP (predEnv x)"
    thus "A x (predEnv x)" using prem1 P2_1_predEnv_def prem2 cons_to_predEnv_def
    by (metis P2_1_cons_def predEnv_substate predEnv_toEnvNum)
  qed
qed

lemma P2_1_lemma:
  assumes "toEnvP s \<and> toEnvP s' \<and> s= predEnv s'"
  and "P2_1_cons A2 s"
  and "A2 s' s"
shows "P2_1_cons A2 s'"
proof -
  have 0:"P2_1_predEnv A2 s" using assms P2_1_bridge by auto
  have "P2_1_predEnv A2 s'"
  proof(simp add: P2_1_predEnv_def; rule allI; rule impI)
    fix x::state
    assume prems:"toEnvP x \<and> substate x s' \<and> toEnvP (predEnv x)"
    show "A2 x (predEnv x)"
    proof (cases)
      assume "x=s'"
      thus ?thesis using assms by auto
    next
      assume 1:"x\<noteq>s'"
      then have "substate x s \<or> (substate s x \<and> substate x s' \<and> x \<noteq> s \<and> x \<noteq> s')" using substate_trans substate_asym substate_eq_or_predEnv
      using assms(1) prems by blast
      then show ?thesis 
      by (metis P2_1_predEnv_def assms(1) 0 prems substate_eq_or_predEnv)
    qed
  qed
  thus ?thesis using P2_1_bridge by auto
qed

section "Pattern 3"

definition P3_cons where
"P3_cons A s = (\<forall>x y z. toEnvP y \<and> toEnvP x \<and> toEnvP z \<and> substate x y \<and> substate y z \<and> substate z s \<and> toEnvNum x y =1 \<longrightarrow> A z y x)"

definition p_2_3_conpred where 
"p_2_3_conpred A y x \<equiv>  A y x (predEnv x)"

definition P3_predEnv where
"P3_predEnv A s = (\<forall>x y. toEnvP y \<and> toEnvP x  \<and> substate x y \<and> substate y s \<and> toEnvP (predEnv x)\<longrightarrow> A y x (predEnv x))"

lemma P3_bridge:
"P3_cons A s = P3_predEnv A s"
proof
  assume prem1:"P3_cons A s"
  thus "P3_predEnv A s"
  proof (simp only:P3_cons_def P3_predEnv_def; intro allI; intro impI)
    fix x y
    assume prem2:"toEnvP y \<and>
           toEnvP x \<and>
           substate x y \<and>
           substate y s \<and>
           toEnvP (predEnv x)"
    thus "A y x (predEnv x)" using prem1 P3_predEnv_def prem2  P3_cons_def predEnv_substate predEnv_toEnvNum p_2_3_conpred_def
   by (metis (no_types, lifting))
  qed
next
  assume prem1:"P3_predEnv A s"
  thus "P3_cons A s"
  proof (simp only:P3_cons_def P3_predEnv_def; intro allI; intro impI)
    fix x y z
    assume prem2:"toEnvP y \<and>
                   toEnvP x \<and>
                   toEnvP z \<and>
                   substate x y \<and>
                   substate y z \<and>
                   substate z s \<and>
                   toEnvNum x y = 1"
    then have "x = predEnv y" using prem2 toEnvNum_predEnv by auto
    thus "A z y x" using prem1 P3_predEnv_def prem2 p_2_3_conpred_def by metis
  qed
qed

lemma P3_lemma:
  assumes "toEnvP s \<and> toEnvP s' \<and> s = predEnv s'"
  and "P3_cons A3 s"
  and "P1 (p_2_3_conpred A3 s') s"
  and "(p_2_3_conpred A3 s') s'"
shows "P3_cons A3 s'"
proof -
  have 1:"P3_predEnv A3 s" using P3_bridge assms(2) by auto
  have "P3_predEnv A3 s'"
  proof (simp add:P3_predEnv_def; intro allI; rule impI)
    fix x y
    assume prems:"toEnvP y \<and> toEnvP x \<and>
           substate x y \<and> substate y s' \<and>
           toEnvP (predEnv x)"
    show "A3 y x (predEnv x)" 
      by (metis "1" P1_def P3_predEnv_def assms(1) assms(3) assms(4) p_2_3_conpred_def prems substate_eq_or_predEnv)
  qed
  thus ?thesis using P3_bridge by auto
qed



section "Pattern 4"

subsection "Pattern 4.1"

definition P4_1 where
"P4_1 A B s \<equiv> (\<forall>s1. toEnvP s1 \<and> substate s1 s \<and> A s s1 \<longrightarrow> (\<exists>s2. toEnvP s2 \<and> substate s2 s1 \<and> B s1 s2))"

definition AB_comb_before where
"AB_comb_before A B s s1 \<equiv> A s s1 \<longrightarrow> (\<exists>s2. toEnvP s2 \<and> substate s2 s1 \<and> B s1 s2)"

lemma P1f_from_P4_1:
  assumes "P4_1 A B s"
  shows "P1f (AB_comb_before A B) s"
  using P1f_def assms
  by (simp add: AB_comb_before_def P4_1_def) 

lemma P4_1_from_P1f:
  assumes "P1f (AB_comb_before A B) s"
  shows "P4_1 A B s"
  using  P1f_def assms 
  by (simp add: AB_comb_before_def P4_1_def)

lemma AB_before_inter:
  fixes A B :: "state\<Rightarrow>state\<Rightarrow>bool"
  assumes "toEnvP s \<and> toEnvP s' \<and> s = predEnv s'"
  and "(\<forall>x. toEnvP x \<and> substate x s' \<and> A s' x \<and> \<not> A s x \<longrightarrow> (\<exists>y. toEnvP y \<and> substate y x \<and> substate y s' \<and> B x y))"
shows "(\<forall>x. toEnvP x \<and> substate x s \<and> (AB_comb_before A B) s x \<longrightarrow> (AB_comb_before A B) s' x)" using AB_comb_before_def assms 
  by (smt (verit, best) predEnv_substate substate_trans)

lemma P4_1_lemma:
  assumes "toEnvP s \<and> toEnvP s' \<and> s=predEnv s'"
    and "P4_1 A B s"
    and "(AB_comb_before A B) s' s'"
    and "(\<forall>x. toEnvP x \<and> substate x s' \<and> A s' x \<and> \<not> A s x \<longrightarrow> (\<exists>y. toEnvP y \<and> substate y x \<and> substate y s' \<and> B x y))"
  shows "P4_1 A B s'" using P1f_from_P4_1 P4_1_from_P1f P1f_lemma assms AB_before_inter by blast

subsection "Pattern 4.2"

definition P4_2 where
"P4_2 A B s \<equiv> (\<forall>s1. toEnvP s1 \<and> substate s1 s \<and> A s s1 \<longrightarrow> 
  (\<exists>s2. toEnvP s2 \<and> substate s1 s2 \<and> substate s2 s\<and> B s1 s2))"

definition AB_comb_after where
"AB_comb_after A B s s1 \<equiv> A s s1 \<longrightarrow> (\<exists>s2. toEnvP s2 \<and> substate s1 s2 \<and> substate s2 s \<and> B s1 s2)"

lemma P1f_from_P4_2:
  assumes "P4_2 A B s"
  shows "P1f (AB_comb_after A B) s"
  using P1f_def assms 
  by (simp add: AB_comb_after_def P4_2_def) 

lemma P4_2_from_P1f:
  assumes "P1f (AB_comb_after A B) s"
  shows "P4_2 A B s"
  using P1f_def assms 
  by (simp add: AB_comb_after_def P4_2_def)

(*
lemma P4_2_lemma:
  assumes "toEnvP s \<and> toEnvP s' \<and> s=predEnv s'"
    and "P4_2 A B s"
    and "(AB_comb_after A B) s' s'"
    and "(\<forall>x. toEnvP x \<and> substate x s \<and> (AB_comb_after A B) s x \<longrightarrow> (AB_comb_after A B) s' x)"
  shows "P4_2 A B s'" using P1f_from_P4_2 P4_2_from_P1f P1f_lemma assms by blast
*)
lemma AB_after_inter:
  fixes A B :: "state\<Rightarrow>state\<Rightarrow>bool"
  assumes "toEnvP s \<and> toEnvP s' \<and> s = predEnv s'"
  and "(\<forall>x. toEnvP x \<and> substate x s' \<and> A s' x \<and> \<not> A s x \<longrightarrow> (\<exists>y. toEnvP y \<and> substate x y \<and> substate y s' \<and> B x y))"
shows "(\<forall>x. toEnvP x \<and> substate x s \<and> (AB_comb_after A B) s x \<longrightarrow> (AB_comb_after A B) s' x)" using AB_comb_after_def assms 
  by (smt (verit, best) predEnv_substate substate_trans)

lemma P4_2_lemma:
  assumes "toEnvP s \<and> toEnvP s' \<and> s=predEnv s'"
    and "P4_2 A B s"
    and "(AB_comb_after A B) s' s'"
    and "(\<forall>x. toEnvP x \<and> substate x s' \<and> A s' x \<and> \<not> A s x \<longrightarrow> (\<exists>y. toEnvP y \<and> substate x y \<and> substate y s' \<and> B x y))"
  shows "P4_2 A B s'" using P1f_from_P4_2 P4_2_from_P1f P1f_lemma assms AB_after_inter by blast

section "Pattern 5"

subsection "Pattern 5.1"

definition P5_1_cons where
"P5_1_cons A B s \<equiv> (\<forall>s1. toEnvP s1 \<and> substate s1 s \<and> A s s1 \<longrightarrow> (\<exists>s2 s3. toEnvP s2 \<and> toEnvP s3 \<and> substate s2 s3 \<and>substate s3 s1 \<and> toEnvNum s2 s3 = 1 \<and> B s1 s3 s2))"

definition P5_1_predEnv where
"P5_1_predEnv A B s \<equiv> (\<forall>s1. toEnvP s1 \<and> substate s1 s \<and> A s s1 \<longrightarrow> (\<exists>s2. toEnvP s2 \<and> substate s2 s1 \<and> toEnvP (predEnv s2) \<and>B s1 s2 (predEnv s2)))"

lemma P5_1_bridge:
"P5_1_cons A B s = P5_1_predEnv A B s"
proof 
  assume prem1:"P5_1_cons A B s"
  thus "P5_1_predEnv A B s"
  proof (simp only:P5_1_cons_def P5_1_predEnv_def; intro allI; intro impI)
    fix s1
    assume prem2:"toEnvP s1 \<and> substate s1 s \<and> A s s1"
    thus "(\<exists>s2. toEnvP s2 \<and> substate s2 s1 \<and> toEnvP (predEnv s2) \<and> B s1 s2 (predEnv s2))" 
      using prem1 P5_1_cons_def P5_1_predEnv_def  p_2_3_conpred_def
    by (smt (verit) toEnvNum_predEnv)
  qed
next
  assume prem1:"P5_1_predEnv A B s"
  thus "P5_1_cons A B s"
  proof (simp only:P5_1_cons_def P5_1_predEnv_def; intro allI; intro impI)
    fix s1
    assume prem2:"toEnvP s1 \<and> substate s1 s \<and> A s s1"
    thus "(\<exists>s2 s3. toEnvP s2 \<and> toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s1 \<and> toEnvNum s2 s3 = 1 \<and> B s1 s3 s2)"
      using prem1 P5_1_cons_def P5_1_predEnv_def predEnv_substate predEnv_toEnvNum p_2_3_conpred_def
    by (metis (no_types, lifting)) 
  qed
qed

definition P5_B_wrap:
"P5_B_wrap B \<equiv> (\<lambda> x y. toEnvP (predEnv y) \<and> B x y (predEnv y))"

lemma P5_1_bridge_P4_1:
"P5_1_predEnv A B s = P4_1 A (P5_B_wrap B) s"
proof
  assume "P5_1_predEnv A B s"
  thus "P4_1 A (P5_B_wrap B) s" using P5_1_predEnv_def P4_1_def P5_B_wrap by metis
next
  assume "P4_1 A (P5_B_wrap B) s"
  thus "P5_1_predEnv A B s" using P5_1_predEnv_def P4_1_def P5_B_wrap by metis
qed


lemma P5_1_lemma:
  assumes "toEnvP s \<and> toEnvP s' \<and> s = predEnv s'"
    and "P5_1_cons A B s"
    and "(AB_comb_before A (P5_B_wrap B)) s' s'"
    and "(\<forall>x. toEnvP x \<and> substate x s' \<and> A s' x \<and> \<not> A s x \<longrightarrow> (\<exists>y. toEnvP y \<and> substate y x \<and> substate y s' \<and> (P5_B_wrap B) x y))"
  shows "P5_1_cons A B s'" using assms P5_1_bridge AB_before_inter substate_eq_or_predEnv P5_B_wrap P5_1_bridge_P4_1 P4_1_lemma by fastforce


(*
"toEnvP s \<and> toEnvP s' \<and> s=predEnv s'"
    and "P4_1 A B s"
    and "(AB_comb_before A B) s' s'"
    and "(\<forall>x. toEnvP x \<and> substate x s' \<and> A s' x \<and> \<not> A s x \<longrightarrow> (\<exists>y. toEnvP y \<and> substate y x \<and> substate y s' \<and> B x y))"
  shows "P4_1 A B s'" using P1f_from_P4_1 P4_1_from_P1f P1f_lemma assms AB_before_inter by blast

*)
section "Pattern 6"

definition P6_cons where
"P6_cons A B C s \<equiv> 
(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> A s s1 \<longrightarrow> 
  (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s1 \<and> toEnvNum s2 s3 = 1 \<and> B s1 s3 s2 \<and>
    (\<forall> s4. toEnvP s4 \<and> substate s3 s4 \<and> substate s4 s1 \<longrightarrow> C s4)))"

definition P6_predEnv where
"P6_predEnv A B C s \<equiv> 
(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> A s s1 \<longrightarrow> 
  (\<exists> s2. toEnvP s2  \<and> substate s2 s1  \<and> B s1 s2 (predEnv s2) \<and> (toEnvP (predEnv s2))\<and>
    (\<forall> s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s1 \<longrightarrow> C s3)))"

definition BC_to_BP4 where
"BC_to_BP4 B C  \<equiv> (\<lambda>s1 s2. B s1 s2 (predEnv s2) \<and> (toEnvP (predEnv s2))\<and>
                    (\<forall> s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s1 \<longrightarrow> C s3))"

lemma P6_bridge:
"P6_cons A B C s = P6_predEnv A B C s"
proof
  assume "P6_cons A B C s"
  thus "P6_predEnv A B C s"
    using P6_cons_def P6_predEnv_def 
    by (smt (verit, ccfv_threshold) toEnvNum_predEnv)
next
  assume "P6_predEnv A B C s"
  thus "P6_cons A B C s"
using P6_cons_def P6_predEnv_def 
  by (smt (verit) predEnv_substate predEnv_toEnvNum)
qed

lemma P6_bridge_P4_1:
"P6_predEnv A B C s = P4_1 A (BC_to_BP4 B C) s"
  apply (simp only: P6_predEnv_def P4_1_def BC_to_BP4_def)
  done

lemma P6_lemma:
  assumes "toEnvP s \<and> toEnvP s' \<and> s = predEnv s'"
    and "P6_cons A B C s"
    and "(AB_comb_before A (BC_to_BP4 B C)) s' s'"
    and "(\<forall>x. toEnvP x \<and> substate x s' \<and> A s' x \<and> \<not> A s x \<longrightarrow> 
          (\<exists>y. toEnvP y \<and> substate y x \<and> substate y s' \<and> (BC_to_BP4 B C) x y))"
  shows "P6_cons A B C s'"
  using P6_bridge_P4_1 P4_1_lemma assms AB_before_inter P6_bridge P6_bridge by auto

(*
(\<forall>x. toEnvP x \<and> substate x s' \<and> A s' x \<and> \<not> A s x \<longrightarrow> 
  (\<exists>y. toEnvP y \<and> substate y x \<and> substate y s' \<and> 
    (substate y x \<and> B (predEnv y) y \<and> (predEnv y \<noteq>emptyState)\<and>
                    (\<forall> s3. toEnvP s3 \<and> substate y s3 \<and> substate s3 x \<longrightarrow> C s3)) ))
*)

section "Pattern 7"
definition P7_cons where
"P7_cons A B s \<equiv>
(\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> substate s1 s2 \<and> substate s2 s \<and> toEnvNum s1 s2 = 1 \<and> A s s2 s1 \<longrightarrow>
  (\<exists> s3. toEnvP s3 \<and> substate s2 s3  \<and> substate s3 s \<and> B s2 s1 s3))"

definition P7_pred where
"P7_pred A B s \<equiv>
(\<forall> s1. toEnvP s1  \<and> substate s1 s \<and> toEnvP (predEnv s1) \<and> A s s1 (predEnv s1)\<longrightarrow>
  (\<exists> s3. toEnvP s3 \<and> substate s1 s3  \<and> substate s3 s \<and> B s1 (predEnv s1) s3))"

definition p_1_2_conpred where
"p_1_2_conpred B x \<equiv> B x (predEnv x)"

section "Pattern 8"
definition P8_cons where
"P8_cons A B C s \<equiv>
(\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> substate s1 s2 \<and> substate s2 s \<and> toEnvNum s1 s2 = 1 \<and> A s s2 s1 \<longrightarrow>
  (\<exists> s4. toEnvP s4 \<and> substate s2 s4  \<and> substate s4 s \<and> B s2 s1 s4 \<and>
    (\<forall>s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s4 \<longrightarrow> C s2 s4 s3)))"

definition P8_pred where
"P8_pred A B C s \<equiv>
(\<forall> s1. toEnvP s1  \<and> substate s1 s \<and> toEnvP (predEnv s1) \<and> (p_2_3_conpred A) s s1 \<longrightarrow>
  (\<exists> s4. toEnvP s4 \<and> substate s1 s4  \<and> substate s4 s \<and> (p_1_2_conpred B) s1 s4 \<and>
    (\<forall>s3. toEnvP s3 \<and> substate s1 s3 \<and> substate s3 s4 \<longrightarrow> C s1 s4 s3)))"

lemma P8_bridge:
"P8_cons A B C s = P8_pred A B C s"
proof 
  assume prem: "P8_cons A B C s" 
  show "P8_pred A B C s" 
  proof(simp only:P8_pred_def;intro allI;intro impI)
    fix s1
    assume "toEnvP s1 \<and>
          substate s1 s \<and>
          toEnvP (predEnv s1) \<and>
          (p_2_3_conpred A) s s1"
    thus "(\<exists>s4. toEnvP s4 \<and> substate s1 s4 \<and> substate s4 s \<and> (p_1_2_conpred B) s1 s4 \<and>
            (\<forall>s3. toEnvP s3 \<and> substate s1 s3 \<and>substate s3 s4 \<longrightarrow> C s1 s4 s3))" 
      using prem P8_cons_def p_1_2_conpred_def p_2_3_conpred_def 
    by (metis (mono_tags, lifting) predEnv_substate predEnv_toEnvNum)
  qed
next
  assume prem:"P8_pred A B C s"
  show "P8_cons A B C s"
  proof (simp only:P8_cons_def;intro allI;intro impI)
    fix s1 s2
    assume "toEnvP s1 \<and>
             toEnvP s2 \<and>
             substate s1 s2 \<and>
             substate s2 s \<and>
             toEnvNum s1 s2 = 1 \<and> A s s2 s1"
    thus "\<exists>s4. toEnvP s4 \<and> substate s2 s4 \<and> substate s4 s \<and> B s2 s1 s4 \<and>
            (\<forall>s3. toEnvP s3 \<and> substate s2 s3 \<and>substate s3 s4 \<longrightarrow> C s2 s4 s3)"
      using prem P8_pred_def p_1_2_conpred_def p_2_3_conpred_def
    by (metis (mono_tags, lifting) toEnvNum_predEnv)
  qed
qed

(*
definition P8_AB_comb:
"P8_AB_comb A B s s1 \<equiv> toEnvP (predEnv s1)\<and> A s s1 (predEnv s1)\<longrightarrow> (\<exists>s2. toEnvP s2 \<and> substate s1 s2 \<and> substate s2 s \<and> B s1 s2)"
*)

definition P8_BC_comb:
"P8_BC_comb B C s1 s4 \<equiv> 
  ((p_1_2_conpred B) s1 s4 \<and>
    (\<forall>s3. toEnvP s3 \<and> substate s1 s3 \<and> substate s3 s4 \<longrightarrow> C s1 s4 s3))"

definition P8_ABC_comb:
"P8_ABC_comb A B C \<equiv> 
  (\<lambda>s s1. AB_comb_after (\<lambda>x1 x2. toEnvP (predEnv s1) \<and> p_2_3_conpred A x1 x2) (P8_BC_comb B C) s s1)"

lemma P8_pred_bridge_P1f:
"P8_pred A B C s = P1f (P8_ABC_comb A B C) s"
proof 
  assume pred:"P8_pred A B C s"
  show "P1f (P8_ABC_comb A B C) s"
    apply (simp only: P1f_def P8_ABC_comb P8_BC_comb AB_comb_after_def P8_pred_def  pred SMT.verit_bool_simplify(4) conj_assoc p_2_3_conpred_def)
  proof (rule allI; rule impI)
    fix s1
    assume 1:"toEnvP s1 \<and> substate s1 s \<and> toEnvP (predEnv s1) \<and>A s s1 (predEnv s1) "
    from pred 1 show "\<exists>s2. toEnvP s2 \<and>substate s1 s2 \<and>substate s2 s \<and> (p_1_2_conpred B) s1 s2 \<and>
           (\<forall>s3. toEnvP s3 \<and>substate s1 s3 \<and>substate s3 s2 \<longrightarrow>C s1 s2 s3)" 
      using pred P8_pred_def 
      by (simp add: p_2_3_conpred_def)
  qed
next
  assume prem:"P1f (P8_ABC_comb A B C) s"
  show "P8_pred A B C s"
  proof (simp only: P8_pred_def; rule allI; rule impI)
    fix s1
    assume "toEnvP s1 \<and>
          substate s1 s \<and>
          toEnvP (predEnv s1) \<and>
          p_2_3_conpred A s s1"
    thus "\<exists>s4. toEnvP s4 \<and> substate s1 s4 \<and> substate s4 s \<and> p_1_2_conpred B s1 s4 \<and>
               (\<forall>s3. toEnvP s3 \<and> substate s1 s3 \<and> substate s3 s4 \<longrightarrow> C s1 s4 s3)"
      using prem P1f_def P8_ABC_comb P8_BC_comb
      by (simp add: AB_comb_after_def p_1_2_conpred_def)
  qed
qed

lemma P8_lemma:
  assumes "toEnvP s \<and> toEnvP s' \<and> s=predEnv s'"
    and "P8_cons A B C s"
    and "(P8_ABC_comb A B C) s' s'"
    and "(\<forall>x. toEnvP x \<and> substate x s' \<and> (p_2_3_conpred A) s' x \<and> \<not> (p_2_3_conpred A) s x \<longrightarrow> 
          (\<exists>y. toEnvP y \<and> substate x y \<and> substate y s' \<and> (P8_BC_comb B C) x y))"
  shows "P8_cons A B C s'"
proof -
  have "toEnvP s \<and> toEnvP s' \<and> s=predEnv s'" using assms by auto
  moreover have "P1f (P8_ABC_comb A B C) s" using assms P8_pred_bridge_P1f P8_bridge by auto
  moreover have "(P8_ABC_comb A B C) s' s'" using assms by auto
  moreover have "(\<forall>x. toEnvP x \<and> substate x s \<and> (P8_ABC_comb A B C) s x \<longrightarrow> (P8_ABC_comb A B C) s' x)" using assms AB_after_inter P8_ABC_comb by auto
  ultimately have "P1f (P8_ABC_comb A B C) s'" using P1f_lemma by auto
  thus ?thesis using P8_pred_bridge_P1f P8_bridge by auto
qed
end