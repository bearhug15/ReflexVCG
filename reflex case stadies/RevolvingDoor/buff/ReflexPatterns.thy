theory ReflexPatterns
  imports Reflex
begin

section "Pattern 1"

definition P1 :: "(state\<Rightarrow>bool)\<Rightarrow>state\<Rightarrow>bool" where
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

definition P1f:: "(state\<Rightarrow>state\<Rightarrow>bool)\<Rightarrow>state\<Rightarrow>bool" where
"P1f A s \<equiv>(\<forall>x. toEnvP x \<and> substate x s \<longrightarrow> A s x)"

lemma P1f_lemma:
  fixes A1 :: "state\<Rightarrow>state\<Rightarrow>bool"
  assumes "toEnvP s \<and> toEnvP s' \<and> s= predEnv s'"
  and "P1f A1 s"
  and "A1 s' s'"
  and "(\<forall>x. toEnvP x \<and> substate x s \<and> A1 s x \<longrightarrow> A1 s' x)"
shows "P1f A1 s'" 
  using P1f_def assms substate_eq_or_predEnv by blast 


section "Pattern 2"

definition P2 :: "(state\<Rightarrow>state\<Rightarrow>bool)\<Rightarrow>state\<Rightarrow>bool" where
"P2 A s = (\<forall>x y. toEnvP y \<and> toEnvP x \<and> substate x y \<and> substate y s \<longrightarrow> A y x)"

lemma P2_lemma:
  fixes A2 :: "state\<Rightarrow>state\<Rightarrow>bool"
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

definition P2_1_cons :: "(state\<Rightarrow>state\<Rightarrow>bool)\<Rightarrow>state\<Rightarrow>bool" where
"P2_1_cons A s \<equiv> (\<forall>x y. toEnvP y \<and> toEnvP x \<and> substate x y \<and> substate y s \<and> toEnvNum x y =1 \<longrightarrow> A y x)"

definition cons_to_predEnv :: "(state\<Rightarrow>state\<Rightarrow>bool) \<Rightarrow> (state\<Rightarrow>bool)" where
"cons_to_predEnv A = (\<lambda> s. A s (predEnv s))"

definition P2_1_predEnv :: "(state\<Rightarrow>state\<Rightarrow>bool)\<Rightarrow>state\<Rightarrow>bool" where
"P2_1_predEnv A s \<equiv> (\<forall>x.  toEnvP x \<and> substate x s \<and> (predEnv x)\<noteq>emptyState \<longrightarrow> A x (predEnv x))"

lemma P2_1_cons_from_predEnv:
  "P2_1_predEnv A s \<Longrightarrow> P2_1_cons A s"
proof -
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
qed

lemma P2_1_cons_to_predEnv:
  "P2_1_cons A s \<Longrightarrow> P2_1_predEnv A s"
proof -
  assume prem1:"P2_1_cons A s"
  thus "P2_1_predEnv A s"
  proof (simp only:P2_1_cons_def P2_1_predEnv_def; intro allI; intro impI)
    fix x
    assume prem2:"toEnvP x \<and>
                   substate x s \<and>
                   toEnvP (predEnv x)"
    thus "A x (predEnv x)" using prem1 P2_1_predEnv_def prem2 cons_to_predEnv_def
    by (metis P2_1_cons_def predEnvP_or_emptyState predEnv_substate predEnv_toEnvNum)
  qed
qed

lemma P2_1_lemma:
  fixes A2 :: "state\<Rightarrow>state\<Rightarrow>bool"
  assumes "toEnvP s \<and> toEnvP s' \<and> s= predEnv s'"
  and "P2_1_predEnv A2 s"
  and "A2 s' s"
shows "P2_1_predEnv A2 s'"
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
    by (metis P2_1_predEnv_def assms(1) assms(2) prems substate_eq_or_predEnv)
  qed
qed

section "Pattern 3"

definition P3_cons :: "(state\<Rightarrow>state\<Rightarrow>state\<Rightarrow>bool)\<Rightarrow>state\<Rightarrow>bool" where
"P3_cons A s \<equiv> (\<forall>x y z. toEnvP y \<and> toEnvP x \<and> toEnvP z \<and> substate x y \<and> substate y z \<and> substate z s \<and> toEnvNum x y =1 \<longrightarrow> A z y x)"

definition p_2_3_conpred :: "(state\<Rightarrow>state\<Rightarrow>state\<Rightarrow>bool) \<Rightarrow> (state\<Rightarrow>state\<Rightarrow>bool)" where
"p_2_3_conpred A \<equiv> (\<lambda> y x. A y x (predEnv x))"
declare p_2_3_conpred_def [simp]

definition P3_predEnv :: "(state\<Rightarrow>state\<Rightarrow>state\<Rightarrow>bool)\<Rightarrow>state\<Rightarrow>bool" where
"P3_predEnv A s \<equiv> (\<forall>x y. toEnvP y \<and> toEnvP x  \<and> substate x y \<and> substate y s \<and> (predEnv x)\<noteq>emptyState\<longrightarrow> p_2_3_conpred A y x)"

lemma P3_bridge:
"P3_predEnv A s \<Longrightarrow> P3_cons A s"
proof -
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
    thus "A z y x" using prem1 P3_predEnv_def prem2 by force
  qed
qed

lemma P3_bridge:
  "P3_cons A s \<Longrightarrow> P3_predEnv A s"
proof -
  assume prem1:"P3_cons A s"
  thus "P3_predEnv A s"
  proof (simp only:P3_cons_def P3_predEnv_def; intro allI; intro impI)
    fix x y
    assume prem2:"toEnvP y \<and>
           toEnvP x \<and>
           substate x y \<and>
           substate y s \<and>
           toEnvP (predEnv x)"
    thus "p_2_3_conpred A y x" using prem1 P3_predEnv_def prem2  P3_cons_def predEnvP_or_emptyState predEnv_substate predEnv_toEnvNum p_2_3_conpred_def
   by (metis (no_types, lifting))
  qed
qed

lemma P3_predEnv:
  assumes "toEnvP s \<and> toEnvP s' \<and> s = predEnv s'"
  and "P3_predEnv A3 s"
  and "A1 = p_2_3_conpred A3 s'"
  and "P1 A1 s"
  and "A1 s'"
shows "P3_predEnv A3 s'"
  by (smt (verit) P1_def P3_predEnv_def assms(1) assms(2) assms(3) assms(4) assms(5) substate_eq_or_predEnv)



section "Pattern 4"

definition P1f_E1f :: "(state\<Rightarrow>state\<Rightarrow>bool) \<Rightarrow> (state\<Rightarrow>state\<Rightarrow>state\<Rightarrow>bool) \<Rightarrow>state \<Rightarrow>bool" where
"P1f_E1f A B s \<equiv> (\<forall>s1. toEnvP s1 \<and> substate s1 s \<and> A s s1 \<longrightarrow> (\<exists>s2. toEnvP s2 \<and> substate s1 s2 \<and> substate s2 s \<and> B s s1 s2))"

definition AB_combf where
"AB_combf A B s s1 \<equiv> A s s1 \<longrightarrow> (\<exists>s2. toEnvP s2 \<and> substate s1 s2 \<and> substate s2 s \<and> B s s1 s2)"

lemma P1f_from_P1f_E1f:
  assumes "P1f_E1f A B s"
  shows "P1f (AB_combf A B) s"
  using AB_combf_def P1f_E1f_def P1f_def assms by auto

lemma P1f_E1f_from_P1f:
  assumes "P1f (AB_combf A B) s"
  shows "P1f_E1f A B s"
  using AB_combf_def P1f_E1f_def P1f_def assms by auto

definition P1f_E1 :: "(state\<Rightarrow>state\<Rightarrow>bool) \<Rightarrow> (state\<Rightarrow>state\<Rightarrow>bool) \<Rightarrow>state \<Rightarrow>bool" where
"P1f_E1 A B s \<equiv> (\<forall>s1. toEnvP s1 \<and> substate s1 s \<and> A s s1 \<longrightarrow> (\<exists>s2. toEnvP s2 \<and> substate s1 s2 \<and> substate s2 s \<and> B s1 s2))"

definition AB_comb where
"AB_comb A B s s1 \<equiv> A s s1 \<longrightarrow> (\<exists>s2. toEnvP s2 \<and> substate s1 s2 \<and> substate s2 s \<and> B s1 s2)"

lemma P1f_from_P1_E1:
  assumes "P1f_E1 A B s"
  shows "P1f (AB_comb A B) s"
  using AB_comb_def P1f_E1_def P1f_def assms by auto

lemma P1f_E1_from_P1f:
  assumes "P1f (AB_comb A B) s"
  shows "P1f_E1 A B s"
  using AB_comb_def P1f_E1_def P1f_def assms by auto

lemma
  assumes "toEnvP s \<and> toEnvP s' \<and> s=predEnv s'"
    and "P1f_E1 A B s"
    and "(AB_comb A B) s' s'"
    and "(\<forall>x. toEnvP x \<and> substate x s \<and> (AB_comb A B) s x \<longrightarrow> (AB_comb A B) s' x)"
  shows "P1f_E1 A B s'" using P1f_from_P1_E1 P1f_E1_from_P1f P1f_lemma assms by blast

lemma
  fixes A B :: "state\<Rightarrow>state\<Rightarrow>bool"
  assumes "toEnvP s \<and> toEnvP s' \<and> s=predEnv s'"
  and "(\<forall>x. toEnvP x \<and> substate x s' \<and> A s' x \<and> \<not> A s x \<longrightarrow> (\<exists>y. toEnvP x \<and> substate x y \<and> substate y s' \<and> B x y))"
shows "(\<forall>x. toEnvP x \<and> substate x s \<and> (AB_comb A B) s x \<longrightarrow> (AB_comb A B) s' x)" using assms AB_comb_def by sledgehammer

(*
(\<forall>x. toEnvP x \<and> substate x s \<and> 
  (A s x \<longrightarrow> (\<exists>s2. toEnvP s2 \<and> substate x s2 \<and> substate s2 s \<and> B x s2)) \<longrightarrow> 
  (A s' x \<longrightarrow> (\<exists>s2. toEnvP s2 \<and> substate x s2 \<and> substate s2 s' \<and> B x s2)))

()
*)
section "Pattern 5"

definition P6_cons :: "(state\<Rightarrow>state\<Rightarrow>bool) \<Rightarrow> (state\<Rightarrow>state\<Rightarrow>bool) \<Rightarrow> (state\<Rightarrow>bool) \<Rightarrow> state \<Rightarrow>bool" where
"P6_cons A B C s \<equiv> 
(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> A s s1 \<longrightarrow> 
  (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s1 \<and> toEnvNum s2 s3 = 1 \<and> B s2 s3 \<and>
    (\<forall> s4. toEnvP s4 \<and> substate s3 s4 \<and> substate s4 s1 \<longrightarrow> C s4)))"

definition P6_predEnv :: "(state\<Rightarrow>state\<Rightarrow>bool) \<Rightarrow> (state\<Rightarrow>state\<Rightarrow>bool) \<Rightarrow> (state\<Rightarrow>bool) \<Rightarrow> state \<Rightarrow>bool" where
"P6_predEnv A B C s \<equiv> 
(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> A s s1 \<longrightarrow> 
  (\<exists> s2. toEnvP s2  \<and> substate s2 s1  \<and> B (predEnv s2) s2 \<and> (toEnvP (predEnv s2))\<and>
    (\<forall> s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s1 \<longrightarrow> C s3)))"

lemma P6_bridge:
"P6_cons A B C s\<Longrightarrow> P6_predEnv A B C s"
using P6_cons_def P6_predEnv_def 
    by (smt (verit, ccfv_threshold) toEnvNum_predEnv toEnvP_emptyState)

lemma P6_bridge:
"P6_predEnv A B C s\<Longrightarrow> P6_cons A B C s"
using P6_cons_def P6_predEnv_def 
  by (smt (verit) predEnvP_or_emptyState predEnv_substate predEnv_toEnvNum)

lemma
  assumes "toEnvP s \<and> toEnvP s' \<and> s = predEnv s'"
    and "P6_predEnv A B C s"
  shows "P6_predEnv A B C s'"

end