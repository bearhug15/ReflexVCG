theory Reflex
  imports Main HOL.Real
begin

type_synonym variable = string

type_synonym process = string

type_synonym pstate = string

datatype state = 
  emptyState 
  | toEnv state
  | setVarBool state variable bool 
  | setVarInt state variable int
  | setVarNat state variable nat
  | setVarReal state variable real
  | setPstate state process pstate 
  | reset state process

primrec getVarBool:: "state \<Rightarrow> variable \<Rightarrow> bool" where
"getVarBool emptyState _ = False"
| "getVarBool (toEnv s) x = getVarBool s x" 
| "getVarBool (setVarBool s y v) x =
  (if x=y then v else (getVarBool s x))" 
| "getVarBool (setVarInt s _ _) x = getVarBool s x" 
| "getVarBool (setVarNat s _ _) x = getVarBool s x"
| "getVarBool (setVarReal s _ _) x = getVarBool s x" 
| "getVarBool (setPstate s _ _) x = getVarBool s x" 
| "getVarBool (reset s _) x = getVarBool s x"

primrec getVarInt:: "state \<Rightarrow> variable \<Rightarrow> int" where
"getVarInt emptyState _ = 0"
| "getVarInt (toEnv s) x = getVarInt s x" 
| "getVarInt (setVarBool s y v) x = getVarInt s x" 
| "getVarInt (setVarInt s y v) x = 
  (if x=y then v else (getVarInt s x))"
| "getVarInt (setVarNat s _ _) x = getVarInt s x"
| "getVarInt (setVarReal s _ _) x = getVarInt s x"
| "getVarInt (setPstate s _ _) x = getVarInt s x" 
| "getVarInt (reset s _) x = getVarInt s x"

primrec getVarNat:: "state \<Rightarrow> variable \<Rightarrow>nat" where
"getVarNat emptyState _ = 0"
| "getVarNat (toEnv s) x = getVarNat s x"
| "getVarNat (setVarBool s _ _) x = getVarNat s x"
| "getVarNat (setVarInt s _ _) x = getVarNat s x"
| "getVarNat (setVarNat s y v) x = 
  (if x=y then v else getVarNat s x)"
| "getVarNat (setVarReal s _ _) x = getVarNat s x"
| "getVarNat (setPstate s _ _) x = getVarNat s x"
| "getVarNat (reset s _) x = getVarNat s x"

primrec getVarReal:: "state \<Rightarrow> variable \<Rightarrow>real" where
"getVarReal emptyState _ = 0.0"
| "getVarReal (toEnv s) x = getVarReal s x"
| "getVarReal (setVarBool s _ _) x = getVarReal s x"
| "getVarReal (setVarInt s _ _) x = getVarReal s x"
| "getVarReal (setVarNat s y v) x = getVarReal s x"
| "getVarReal (setVarReal s y v) x = (if x=y then v else getVarReal s x)"
| "getVarReal (setPstate s _ _) x = getVarReal s x"
| "getVarReal (reset s _) x = getVarReal s x"

primrec getPstate:: "state \<Rightarrow> process \<Rightarrow> pstate" where
"getPstate emptyState _ = ''stop''"
| "getPstate (toEnv s) p = getPstate s p" 
| "getPstate (setVarBool s _ _) p = getPstate s p" 
| "getPstate (setVarInt s _ _) p = getPstate s p"
| "getPstate (setVarNat s _ _) p = getPstate s p"
| "getPstate (setVarReal s _ _) p = getPstate s p"
| "getPstate (setPstate s p1 q) p =
  (if p=p1 then q else (getPstate s p))" 
| "getPstate (reset s _) p = getPstate s p"

primrec substate:: "state \<Rightarrow> state \<Rightarrow> bool" where
"substate s emptyState =
 (if s = emptyState then True else False)" 
| "substate s (toEnv s1) =
  (if s = toEnv s1 then True else substate s s1)" 
| "substate s (setVarBool s1 v u) = 
  (if s = setVarBool s1 v u then True else substate s s1)" 
| "substate s (setVarInt s1 v u) =
  (if s = setVarInt s1 v u then True else substate s s1)"
| "substate s (setVarNat s1 v u) =
  (if s = setVarNat s1 v u then True else substate s s1)"
| "substate s (setVarReal s1 v u) =
  (if s = setVarReal s1 v u then True else substate s s1)"
| "substate s (setPstate s1 p q) =
  (if s = setPstate s1 p q then True else substate s s1)" 
| "substate s (reset s1 p) =
  (if s = reset s1 p then True else substate s s1)"

primrec toEnvNum:: "state \<Rightarrow>state \<Rightarrow> nat" where 
"toEnvNum s emptyState = 0" 
| "toEnvNum s (toEnv s1) = 
  (if s = toEnv s1 then 0 else toEnvNum s s1 + 1)" 
| "toEnvNum s (setVarBool s1 v u) =
  (if s = setVarBool s1 v u then 0 else toEnvNum s s1)" 
| "toEnvNum s (setVarInt s1 v u) =
  (if s = setVarInt s1 v u then 0 else toEnvNum s s1)" 
| "toEnvNum s (setVarNat s1 v u) =
  (if s = setVarNat s1 v u then 0 else toEnvNum s s1)"
| "toEnvNum s (setVarReal s1 v u) =
  (if s = setVarReal s1 v u then 0 else toEnvNum s s1)"
| "toEnvNum s (setPstate s1 p q) = 
  (if s = setPstate s1 p q then 0 else toEnvNum s s1)" 
| "toEnvNum s (reset s1 p) =
  (if s = reset s1 p then 0 else toEnvNum s s1)"
(*
primrec toEnvP::"state \<Rightarrow> bool" where
"toEnvP (toEnv _) = True" 
| "toEnvP emptyState = False"
| "toEnvP (setVarBool _ _ _) = False"
| "toEnvP (setVarNat _ _ _) = False"
| "toEnvP (setVarInt _ _ _) = False"
| "toEnvP (setVarReal _ _ _) = False"
| "toEnvP (setPstate _ _ _) = False"
| "toEnvP (reset _ _ ) = False"
*)
fun toEnvP::"state \<Rightarrow> bool" where
"toEnvP (toEnv _) = True" 
| "toEnvP _ = False"

primrec predEnv:: "state \<Rightarrow> state" where
"predEnv emptyState = emptyState" 
| "predEnv (toEnv s) =
  (if (toEnvP s) then s else predEnv s)" 
| "predEnv (setVarBool s _ _) = 
  (if (toEnvP s) then s else predEnv s)" 
| "predEnv (setVarInt s _ _) = 
  (if (toEnvP s) then s else predEnv s)"
| "predEnv (setVarNat s _ _) = 
  (if (toEnvP s) then s else predEnv s)"
| "predEnv (setVarReal s _ _) = 
  (if (toEnvP s) then s else predEnv s)"
| "predEnv (setPstate s _ _) = 
  (if (toEnvP s) then s else predEnv s)" 
| "predEnv (reset s _) = 
  (if (toEnvP s) then s else predEnv s)"

primrec shiftEnv:: "state \<Rightarrow> nat \<Rightarrow> state" where
"shiftEnv s 0 = s" |
"shiftEnv s (Suc n) = predEnv (shiftEnv s n)"


section "Lemmas"

lemma substate_refl: "substate s s"
  apply(cases s)
         apply(auto)
  done

lemma substate_trans:
"substate s1 s2 \<and> substate s2 s3 \<Longrightarrow> substate s1 s3"
proof(induction s3)
    case emptyState
 assume 1: "substate s1 s2 \<and> substate s2 emptyState"
    hence "s2=emptyState"
    proof(cases)
      assume "s2=emptyState"
      thus ?thesis by assumption
    next
      assume  "s2 \<noteq> emptyState"
      with 1 show ?thesis by simp
    qed
    with 1 show ?case by auto
  next
    case (toEnv s3)
    assume 1: "substate s1 s2 \<and> substate s2 (toEnv s3)"
     show ?case
    proof(cases)
      assume "s2=toEnv s3"
      with 1 show ?case by auto
    next
      assume "s2 \<noteq> toEnv s3"
      with 1 toEnv.IH show ?case by auto
    qed   
  next
    case (setVarBool s3 v u)
      assume 1: "substate s1 s2 \<and> substate s2 (setVarBool s3 v u)"
      show ?case
      proof(cases)
        assume "s2 = setVarBool s3 v u"
        with 1 show ?case by auto
      next
        assume "s2 \<noteq> setVarBool s3 v u"
        with 1 setVarBool.IH show ?case by auto
      qed
  next
    case (setVarInt s3 v u)
    assume 1:  "substate s1 s2 \<and> substate s2 (setVarInt s3 v u)"
    show ?case
    proof(cases)
      assume "s2 = setVarInt s3 v u"
      with 1 show ?case by auto
    next
      assume "s2 \<noteq> setVarInt s3 v u"
      with 1 setVarInt.IH show ?case by auto
    qed
  next
    case (setVarNat s3 v u)
    assume 1:  "substate s1 s2 \<and> substate s2 (setVarNat s3 v u)"
    show ?case
    proof(cases)
      assume "s2 = setVarNat s3 v u"
      with 1 show ?case by auto
    next
      assume "s2 \<noteq> setVarNat s3 v u"
      with 1 setVarNat.IH show ?case by auto
    qed
next
    case (setVarReal s3 v u)
    assume 1:  "substate s1 s2 \<and> substate s2 (setVarReal s3 v u)"
    show ?case
    proof(cases)
      assume "s2 = setVarReal s3 v u"
      with 1 show ?case by auto
    next
      assume "s2 \<noteq> setVarReal s3 v u"
      with 1 setVarReal.IH show ?case by auto
    qed
  next
    case (setPstate s3 p q)
    assume 1: "substate s1 s2 \<and> substate s2 (setPstate s3 p q)"
    show ?case
    proof(cases)
      assume "s2 = setPstate s3 p q"
      with 1 show ?case by auto
    next
      assume "s2 \<noteq> setPstate s3 p q"
      with 1 setPstate.IH show ?case by auto
    qed
  next
    case (reset s3 p)
    assume 1: "substate s1 s2 \<and> substate s2 (reset s3 p)"
    show ?case
    proof(cases)
      assume "s2 = reset s3 p"
      with 1 show ?case by auto
    next
      assume "s2 \<noteq> reset s3 p"
      with 1 reset.IH show ?case by auto
    qed
  qed


lemma substate_asym:
 "substate s1 s2 \<and> substate s2 s1 \<Longrightarrow> s1=s2"
proof(induction s2 arbitrary: s1)
  case emptyState
  assume 1: "substate s1 emptyState \<and> substate emptyState s1"
  show ?case
  proof(cases)
    assume "s1=emptyState"
    thus ?case by assumption
  next
    assume "s1 \<noteq> emptyState"
    with 1 show ?case by auto
  qed
next
  case (toEnv s2)
  assume 1: "substate s1 (toEnv s2) \<and> substate (toEnv s2) s1"
  show ?case
  proof(cases)
    assume "s1 = toEnv s2"
    thus ?case by assumption
  next
    assume 2: "s1 \<noteq> toEnv s2"
    from 1 obtain 3: "substate s1 (toEnv s2)" 
and 4: "substate (toEnv s2) s1" ..
    from 2 3 have "substate s1 s2" by simp
    with 4 substate_trans 
    have 5: "substate (toEnv s2) s2" by blast
    have "substate s2 (toEnv s2)" 
      using substate_refl by auto
    with 5 toEnv.IH have "toEnv s2 = s2" by auto
    with 3 4 toEnv.IH show ?case by auto
  qed
next
  case (setVarBool s2 v u)
    assume 1: "substate s1 (setVarBool s2 v u) \<and> substate (setVarBool s2 v u) s1"
  show ?case
  proof(cases)
    assume "s1 = setVarBool s2 v u"
    thus ?case by assumption
  next
    assume 2: "s1 \<noteq> setVarBool s2 v u"
    from 1 obtain 3: "substate s1 (setVarBool s2 v u)" 
and 4: "substate (setVarBool s2 v u) s1" ..
    from 2 3 have "substate s1 s2" by simp
    with 4 substate_trans 
    have 5: "substate (setVarBool s2 v u) s2" by blast
    have "substate s2 (setVarBool s2 v u)" 
      using substate_refl by auto
    with 5 setVarBool.IH have "setVarBool s2 v u = s2" by auto
    with 3 4 setVarBool.IH show ?case by auto
  qed
next
  case (setVarInt s2 v u)
    assume 1: "substate s1 (setVarInt s2 v u) \<and> substate (setVarInt s2 v u) s1"
  show ?case
  proof(cases)
    assume "s1 = setVarInt s2 v u"
    thus ?case by assumption
  next
    assume 2: "s1 \<noteq> setVarInt s2 v u"
    from 1 obtain 3: "substate s1 (setVarInt s2 v u)" 
and 4: "substate (setVarInt s2 v u) s1" ..
    from 2 3 have "substate s1 s2" by simp
    with 4 substate_trans 
    have 5: "substate (setVarInt s2 v u) s2" by blast
    have "substate s2 (setVarInt s2 v u)" 
      using substate_refl by auto
    with 5 setVarInt.IH have "setVarInt s2 v u = s2" by auto
    with 3 4 setVarInt.IH show ?case by auto
  qed
next
  case (setVarNat s2 v u)
    assume 1: "substate s1 (setVarNat s2 v u) \<and> substate (setVarNat s2 v u) s1"
  show ?case
  proof(cases)
    assume "s1 = setVarNat s2 v u"
    thus ?case by assumption
  next
    assume 2: "s1 \<noteq> setVarNat s2 v u"
    from 1 obtain 3: "substate s1 (setVarNat s2 v u)" 
and 4: "substate (setVarNat s2 v u) s1" ..
    from 2 3 have "substate s1 s2" by simp
    with 4 substate_trans 
    have 5: "substate (setVarNat s2 v u) s2" by blast
    have "substate s2 (setVarNat s2 v u)" 
      using substate_refl by auto
    with 5 setVarNat.IH have "setVarNat s2 v u = s2" by auto
    with 3 4 setVarNat.IH show ?case by auto
  qed
next
  case (setVarReal s2 v u)
    assume 1: "substate s1 (setVarReal s2 v u) \<and> substate (setVarReal s2 v u) s1"
  show ?case
  proof(cases)
    assume "s1 = setVarReal s2 v u"
    thus ?case by assumption
  next
    assume 2: "s1 \<noteq> setVarReal s2 v u"
    from 1 obtain 3: "substate s1 (setVarReal s2 v u)" 
and 4: "substate (setVarReal s2 v u) s1" ..
    from 2 3 have "substate s1 s2" by simp
    with 4 substate_trans 
    have 5: "substate (setVarReal s2 v u) s2" by blast
    have "substate s2 (setVarReal s2 v u)" 
      using substate_refl by auto
    with 5 setVarReal.IH have "setVarReal s2 v u = s2" by auto
    with 3 4 setVarReal.IH show ?case by auto
  qed
next
  case (setPstate s2 p q)
    assume 1: "substate s1 (setPstate s2 p q) \<and> substate (setPstate s2 p q) s1"
  show ?case
  proof(cases)
    assume "s1 = setPstate s2 p q"
    thus ?case by assumption
  next
    assume 2: "s1 \<noteq> setPstate s2 p q"
    from 1 obtain 3: "substate s1 (setPstate s2 p q)" 
and 4: "substate (setPstate s2 p q) s1" ..
    from 2 3 have "substate s1 s2" by simp
    with 4 substate_trans 
    have 5: "substate (setPstate s2 p q) s2" by blast
    have "substate s2 (setPstate s2 p q)" 
      using substate_refl by auto
    with 5 setPstate.IH have "setPstate s2 p q = s2" by auto
    with 3 4 setPstate.IH show ?case by auto
  qed
next
  case (reset s2 p)
    assume 1: "substate s1 (reset s2 p) \<and> substate (reset s2 p) s1"
  show ?case
  proof(cases)
    assume "s1 = reset s2 p"
    thus ?case by assumption
  next
    assume 2: "s1 \<noteq> reset s2 p"
    from 1 obtain 3: "substate s1 (reset s2 p)" 
and 4: "substate (reset s2 p) s1" ..
    from 2 3 have "substate s1 s2" by simp
    with 4 substate_trans 
    have 5: "substate (reset s2 p) s2" by blast
    have "substate s2 (reset s2 p)" 
      using substate_refl by auto
    with 5 reset.IH have "reset s2 p = s2" by auto
    with 3 4 reset.IH show ?case by auto
  qed
qed

lemma predEnv_substate: "substate (predEnv s) s"
  apply(induction s)
  using substate_refl  by auto

lemma substate_eq_or_predEnv: 
"substate s1 s2 \<and> toEnvP s1 \<longrightarrow> s1=s2 \<or> substate s1 (predEnv s2)"
  apply(induction s2)
         apply(auto)
  done

lemma substate_total: 
"substate s1 s \<and> substate s2 s \<longrightarrow>
 substate s1 s2 \<or> substate s2 s1"
  apply(induction s)
         apply(auto)
  done

lemma toEnvNum_id: "toEnvNum s s = 0"
  apply(cases s)
         apply(auto)
  done

lemma substate_toEnvNum_id:
"substate s1 s2 \<and> toEnvNum s1 s2 = 0 \<and> toEnvP s1 \<and>
toEnvP s2 \<longrightarrow> s1=s2"
  apply(cases s2)
         apply(auto)
  done

lemma gtime_predI:
 "\<not> toEnvP s \<Longrightarrow>   toEnvNum emptyState s =
 toEnvNum emptyState (predEnv s)"
  apply(induction s)
         apply(auto)
  done

lemma gtime_predE:
"toEnvP s \<longrightarrow> toEnvNum emptyState s =
toEnvNum emptyState (predEnv s) + 1"
proof(induction s)
  case emptyState
  then show ?case by auto
next
  case (toEnv s)
  then show ?case 
  proof cases
    assume "toEnvP s"
    thus ?case by auto
  next
    assume 1: "\<not> toEnvP s"
    have "toEnvNum emptyState (toEnv s) =
toEnvNum emptyState s + 1" by auto
    with 1 gtime_predI show ?thesis by auto
  qed
next
 
  case (setVarBool s x2a x3a)
  then show ?case by auto
next
  case (setVarInt s x2a x3a)
  then show ?case by auto
next
  case (setVarNat s x2a x3a)
  then show ?case by auto
next
  case (setVarReal s x2a x3a)
  then show ?case by auto
next
  case (setPstate s x2a x3a)
  then show ?case by auto
next
  case (reset s x2a)
  then show ?case by auto
qed

lemma predEnvPI: "\<not> toEnvP s \<and>
toEnvNum emptyState s > 0 \<longrightarrow>
toEnvP (predEnv s)"
  apply(induction s)
         apply(auto)
  done

lemma predEnvP: "toEnvNum emptyState s > 1 \<Longrightarrow>
toEnvP (predEnv s)"
proof(induction s)
  case emptyState
  then show ?case by auto
next
  case (toEnv s)
  then show ?case 
  proof(cases)
    assume "toEnvP s"
    thus ?case by auto
  next
    assume 1: "\<not> toEnvP s"
    assume "toEnvNum  emptyState (toEnv s) > 1"
    hence "toEnvNum emptyState s > 0" by auto
    with 1  predEnvPI  show ?case by auto
  qed
next
  case (setVarBool s v u)
  then show ?case 
proof(cases)
    assume "toEnvP s"
    thus ?case by auto
  next
    assume 1: "\<not> toEnvP s"
assume "toEnvNum  emptyState (setVarBool s v u) > 1"
  hence "toEnvNum emptyState s > 1" by auto
  with 1 setVarBool.IH show ?case by auto
qed
next
  case (setVarInt s v u)
  then show ?case 
proof(cases)
    assume "toEnvP s"
    thus ?case by auto
  next
    assume 1: "\<not> toEnvP s"
assume "toEnvNum  emptyState (setVarInt s v u) > 1"
  hence "toEnvNum emptyState s > 1" by auto
  with 1 setVarInt.IH show ?case by auto
qed
next
  case (setVarReal s v u)
  then show ?case 
proof(cases)
    assume "toEnvP s"
    thus ?case by auto
  next
    assume 1: "\<not> toEnvP s"
assume "toEnvNum  emptyState (setVarReal s v u) > 1"
  hence "toEnvNum emptyState s > 1" by auto
  with 1 setVarReal.IH show ?case by auto
qed
next
  case (setVarNat s v u)
  then show ?case 
proof(cases)
    assume "toEnvP s"
    thus ?case by auto
  next
    assume 1: "\<not> toEnvP s"
assume "toEnvNum  emptyState (setVarNat s v u) > 1"
  hence "toEnvNum emptyState s > 1" by auto
  with 1 setVarNat.IH show ?case by auto
qed
next
  case (setPstate s p q)
  then show ?case 
proof(cases)
    assume "toEnvP s"
    thus ?case by auto
  next
    assume 1: "\<not> toEnvP s"
assume "toEnvNum  emptyState (setPstate s p q) > 1"
  hence "toEnvNum emptyState s > 1" by auto
  with 1 setPstate.IH show ?case by auto
qed
next
  case (reset s p)
  then show ?case 
proof(cases)
    assume "toEnvP s"
    thus ?case by auto
  next
    assume 1: "\<not> toEnvP s"
assume "toEnvNum  emptyState (reset s p) > 1"
  hence "toEnvNum emptyState s > 1" by auto
  with 1 reset.IH show ?case by auto
qed
qed


lemma shift_spec:
 "toEnvP s \<and> toEnvNum emptyState s > n \<Longrightarrow>
toEnvP (shiftEnv s n) \<and>
 toEnvNum emptyState (shiftEnv s n) =
 toEnvNum emptyState s - n"
proof(induction n)
  case 0
  then show ?case by auto
next
  case (Suc n)
  assume 1: "toEnvP s \<and>
         Suc n < toEnvNum emptyState s"
  with Suc.IH
  have "toEnvNum emptyState (shiftEnv s n) > 1"
    by auto
  with predEnvP have 2:
 "toEnvP (shiftEnv s (Suc n))"
    by auto
  from 1 Suc.IH have 3:  "toEnvP (shiftEnv s n)"
    by auto
  from 1 Suc.IH have
 "toEnvNum emptyState (shiftEnv s n) =
toEnvNum emptyState s - n" by auto
  with 1 3  gtime_predE have
"toEnvNum emptyState (shiftEnv s (Suc n)) =
toEnvNum emptyState s - (Suc n)" by auto
  with 2 show ?case ..  
qed  

lemma toEnvNum3: "substate s1 s2 \<and> substate s2 s3
 \<Longrightarrow> toEnvNum s1 s3 = toEnvNum s1 s2 + toEnvNum s2 s3"
  proof(induction s3)
    case emptyState
    then show ?case by (cases s2; auto)
  next
    case (toEnv s3)
    then show ?case 
    proof(cases)
      assume "s2= toEnv s3"
      thus ?case by auto
    next
      assume 1: "s2 \<noteq> toEnv s3"
      assume 2:  "substate s1 s2 \<and> substate s2 (toEnv s3)"
      with 1 have "substate s2 s3" by simp
      show ?case
      proof(cases)
        assume "s1 = toEnv s3"
        with 2 substate_asym[of "(toEnv s3)" s2]
        have "toEnv s3 = s2" by auto
        with 1 show ?case by auto
      next
        assume "s1 \<noteq> (toEnv s3)"
        with 1 2 toEnv.IH show?case by auto
      qed
    qed
  next
    case (setVarBool s3 v u)
    then show ?case 
    proof(cases)
      assume "s2= setVarBool s3 v u"
      thus ?case by auto
    next
      assume 1: "s2 \<noteq> setVarBool s3 v u"
      assume 2:  "substate s1 s2 \<and> substate s2
 (setVarBool s3 v u)"
      with 1 have "substate s2 s3" by simp
      show ?case
      proof(cases)
        assume "s1 = setVarBool s3 v u"
        with 2 substate_asym[of "(setVarBool s3 v u)" s2]
        have "setVarBool s3 v u = s2" by auto
        with 1 show ?case by auto
      next
        assume "s1 \<noteq> (setVarBool s3 v u)"
        with 1 2 setVarBool.IH show?case by auto
      qed
    qed
  next
    case (setVarInt s3 v u)
    then show ?case 
    proof(cases)
      assume "s2= setVarInt s3 v u"
      thus ?case by auto
    next
      assume 1: "s2 \<noteq> setVarInt s3 v u"
      assume 2:  "substate s1 s2 \<and> substate s2 (setVarInt s3 v u)"
      with 1 have "substate s2 s3" by simp
      show ?case
      proof(cases)
        assume "s1 = setVarInt s3 v u"
        with 2 substate_asym[of "(setVarInt s3 v u)" s2]
        have "setVarInt s3 v u = s2" by auto
        with 1 show ?case by auto
      next
        assume "s1 \<noteq> (setVarInt s3 v u)"
        with 1 2 setVarInt.IH show?case by auto
      qed
    qed
   next
    case (setVarNat s3 v u)
    then show ?case 
    proof(cases)
      assume "s2= setVarNat s3 v u"
      thus ?case by auto
    next
      assume 1: "s2 \<noteq> setVarNat s3 v u"
      assume 2:  "substate s1 s2 \<and> substate s2 (setVarNat s3 v u)"
      with 1 have "substate s2 s3" by simp
      show ?case
      proof(cases)
        assume "s1 = setVarNat s3 v u"
        with 2 substate_asym[of "(setVarNat s3 v u)" s2]
        have "setVarNat s3 v u = s2" by auto
        with 1 show ?case by auto
      next
        assume "s1 \<noteq> (setVarNat s3 v u)"
        with 1 2 setVarNat.IH show?case by auto
      qed
    qed
   next
    case (setVarReal s3 v u)
    then show ?case 
    proof(cases)
      assume "s2= setVarReal s3 v u"
      thus ?case by auto
    next
      assume 1: "s2 \<noteq> setVarReal s3 v u"
      assume 2:  "substate s1 s2 \<and> substate s2 (setVarReal s3 v u)"
      with 1 have "substate s2 s3" by simp
      show ?case
      proof(cases)
        assume "s1 = setVarReal s3 v u"
        with 2 substate_asym[of "(setVarReal s3 v u)" s2]
        have "setVarReal s3 v u = s2" by auto
        with 1 show ?case by auto
      next
        assume "s1 \<noteq> (setVarReal s3 v u)"
        with 1 2 setVarReal.IH show?case by auto
      qed
    qed
  next
    case (setPstate s3 p q)
    then show ?case 
    proof(cases)
      assume "s2= setPstate s3 p q"
      thus ?case by auto
    next
      assume 1: "s2 \<noteq> setPstate s3 p q"
      assume 2:  "substate s1 s2 \<and> substate s2 (setPstate s3 p q)"
      with 1 have "substate s2 s3" by simp
      show ?case
      proof(cases)
        assume "s1 = setPstate s3 p q"
        with 2 substate_asym[of "(setPstate s3 p q)" s2]
        have "setPstate s3 p q = s2" by auto
        with 1 show ?case by auto
      next
        assume "s1 \<noteq> (setPstate s3 p q)"
        with 1 2 setPstate.IH show?case by auto
      qed
    qed
  next
    case (reset s3 p)
    then show ?case 
    proof(cases)
      assume "s2= reset s3 p"
      thus ?case by auto
    next
      assume 1: "s2 \<noteq> reset s3 p"
      assume 2:  "substate s1 s2 \<and> substate s2 (reset s3 p)"
      with 1 have "substate s2 s3" by simp
      show ?case
      proof(cases)
        assume "s1 = reset s3 p"
        with 2 substate_asym[of "(reset s3 p)" s2]
        have "reset s3 p = s2" by auto
        with 1 show ?case by auto
      next
        assume "s1 \<noteq> (reset s3 p)"
        with 1 2 reset.IH show?case by auto
      qed
    qed
  qed


lemma emptyState_substate: "substate emptyState s"
  apply(induction s)
         apply(auto)
  done


lemma gtimeE_inj:
"substate s1 s \<and> toEnvP s1 \<and> toEnvP s \<and>
toEnvNum emptyState s1 = toEnvNum emptyState s \<longrightarrow>
s1=s"
proof(cases s)
  case emptyState
  then show ?thesis by auto
next
  case (toEnv s2)
  then show ?thesis 
  proof cases
    assume "s1 =s"
    then show ?thesis by auto
  next
    assume 1:  "s1 \<noteq> s"
    show ?thesis 
    proof
      assume 2: "substate s1 s \<and>
    toEnvP s1 \<and>
    toEnvP s \<and>
    toEnvNum emptyState s1 =
    toEnvNum emptyState s"
      with 1 toEnv have 3: "substate s1 s2" by auto
      from toEnv have 4: "substate s2 s" 
        using substate_refl by auto
      from 3 emptyState_substate toEnvNum3 
      have 5:  "toEnvNum emptyState s2 =
toEnvNum emptyState s1 + toEnvNum s1 s2" by auto
      from emptyState_substate 4 toEnvNum3 have 6:
"toEnvNum emptyState s = toEnvNum emptyState s2 + toEnvNum s2 s"
        by auto
      with toEnv  have 7: "toEnvNum s2 s > 0" by auto
      from 5 have
 "toEnvNum emptyState s1 \<le> toEnvNum emptyState s2"
        by auto
      also from 6 7 have
"toEnvNum emptyState s2 < toEnvNum emptyState s"
        by auto
      finally have 
 "toEnvNum emptyState s1 < toEnvNum emptyState s" .
      with 2 show
"substate s1 s \<and>
    toEnvP s1 \<and>
    toEnvP s \<and>
    toEnvNum emptyState s1 =
    toEnvNum emptyState s \<Longrightarrow> s1=s" by auto
    qed
  qed

next
  case (setVarBool x41 x42 x43)
  then show ?thesis by auto
next
  case (setVarInt x51 x52 x53)
  then show ?thesis by auto
next
  case (setVarNat x51 x52 x53)
  then show ?thesis by auto
next
  case (setVarReal x51 x52 x53)
  then show ?thesis by auto
next
  case (setPstate x71 x72 x73)
  then show ?thesis by auto
next
  case (reset x81 x82)
  then show ?thesis by auto
qed

lemma shiftEnv_substate: "substate (shiftEnv s n) s"
proof(induction n)
  case 0
  then show ?case using substate_refl by auto
next
  case (Suc n)
  then have "substate (predEnv (shiftEnv s n)) s"
    using predEnv_substate[of "shiftEnv s n"]
substate_trans by blast
  thus ?case by simp
qed

lemma toEnvNum_shift: "toEnvP s \<and>
toEnvNum emptyState s > n
  \<Longrightarrow> toEnvNum (shiftEnv s n) s = n"
proof -
  assume 1: "toEnvP s \<and> toEnvNum emptyState s > n"
  have 2:  "toEnvNum emptyState s =
toEnvNum emptyState (shiftEnv s n) +
toEnvNum (shiftEnv s n) s" 
    using emptyState_substate toEnvNum3 
shiftEnv_substate by auto
  with 1 shift_spec show ?thesis by force
qed

lemma substate_shift:
"toEnvP s1 \<and> toEnvP s \<and> substate s1 s \<and> 
toEnvNum s1 s \<ge> n \<Longrightarrow> 
substate s1 (shiftEnv s n)"
proof(induction n)
  case 0
  then show ?case by auto
next
  case (Suc n)
  then show ?case
  proof -
    assume 1:  "toEnvP s1 \<and>
    toEnvP s \<and>
    substate s1 s \<and> Suc n \<le> toEnvNum s1 s"
    with Suc.IH have "substate s1 (shiftEnv s n)"
      by auto
    with 1 substate_eq_or_predEnv have
"s1 = shiftEnv s n \<or> 
substate s1 (predEnv (shiftEnv s n))" by auto
    then show ?case
    proof
      assume 2: "s1 = shiftEnv s n"
      with 1 emptyState_substate[of s1]  toEnvNum3
      have  "toEnvNum emptyState s > n" by force
      with 1 toEnvNum_shift have
"toEnvNum (shiftEnv s n) s = n" by auto
      with 1 2 show ?case by auto
    next
      assume "substate s1 (predEnv (shiftEnv s n))"
      thus ?case by simp  
    qed
  qed
qed

lemma predEnvP_or_emptyState:
"toEnvP (predEnv s) \<or> predEnv s = emptyState"
  apply(induction s)
         apply(auto)
  done

lemma shiftEnvP_or_emptyState: "n \<noteq> 0 \<longrightarrow>
 toEnvP (shiftEnv s n) \<or> shiftEnv s n = emptyState"
  apply(cases n)
  using predEnvP_or_emptyState by auto

lemma shift_toEnvNum: 
"toEnvP s \<and> toEnvP s1 \<and> substate s1 s \<Longrightarrow>
shiftEnv s (toEnvNum s1 s) = s1"
proof -
  define s2:: state where
 "s2 = shiftEnv s (toEnvNum s1 s)"
  assume 1: "toEnvP s \<and> toEnvP s1 \<and> substate s1 s"
  with s2_def substate_shift have 2: 
"substate s1 s2" by auto
  from 1 have "toEnvNum emptyState s1 > 0"
    by (cases s1; auto)
  with 1 emptyState_substate[of s1] toEnvNum3 have
"toEnvNum s1 s < toEnvNum emptyState s" by force
  with 1 shift_spec s2_def have 
"toEnvNum emptyState s2 = 
toEnvNum emptyState s - toEnvNum s1 s" by auto
  also from 1 emptyState_substate[of s1] toEnvNum3
  have "toEnvNum  emptyState s - toEnvNum s1 s = 
toEnvNum emptyState s1" by force
  finally have 3:
 "toEnvNum emptyState s2 = toEnvNum emptyState s1"
    .
  show ?thesis
  proof(cases)
    assume 4: "toEnvNum s1 s = 0"
    with 1 substate_toEnvNum_id have "s1=s"
      by blast
    with 1 show ?thesis using toEnvNum_id  by auto
  next
    assume "toEnvNum s1 s \<noteq> 0"
    with s2_def shiftEnvP_or_emptyState have
"toEnvP s2 \<or> s2 = emptyState" by auto
    hence "toEnvP s2" 
    proof
      assume "toEnvP s2"
      thus ?thesis by assumption
    next
      assume "s2 = emptyState"
      with 2 have "s1=emptyState"
        by (cases s1; auto)
      with 1 show ?thesis by auto
    qed
    with 1 2 3[symmetric]  gtimeE_inj
    have "s1=s2" by blast
    with s2_def show ?thesis by simp
  qed
qed

lemma predEnv_substate_imp_eq_or_substate:
"toEnvP s1 \<and> toEnvP s2 \<and> substate s1 s \<and>
 substate s2 s \<and> substate (predEnv s1) s2 \<longrightarrow> 
s2 = predEnv s1 \<or> substate s1 s2"
proof cases
  assume "s1 = s2"
  with substate_refl show ?thesis by auto
next 
  assume 1: "s1 \<noteq> s2"
  show ?thesis
  proof
    assume 2: "toEnvP s1 \<and>
    toEnvP s2 \<and>
    substate s1 s \<and>
    substate s2 s \<and> substate (predEnv s1) s2 "
    with substate_total 
    have "substate s1 s2 \<or> substate s2 s1" by auto
    thus " s2 = predEnv s1 \<or> substate s1 s2"
    proof
      assume "substate s1 s2" 
      thus ?thesis by auto
    next
      assume "substate s2 s1"
      with 1 substate_eq_or_predEnv 2 substate_asym
      show ?thesis by auto
    qed
  qed
qed

lemma state_down_ind: 
"toEnvP s \<and> toEnvP s1 \<Longrightarrow> P s \<Longrightarrow>
 (\<And> s2. toEnvP s2 \<Longrightarrow>
 (substate s1 s2 \<and> substate s2 s \<and> s1 \<noteq> s2)\<Longrightarrow> 
  P s2 \<Longrightarrow> 
  P (predEnv s2)) \<Longrightarrow>
toEnvP s2 \<and> substate s1 s2 \<and> substate s2 s \<longrightarrow>
 P s2"
proof -
  assume 1: "toEnvP s \<and> toEnvP s1"
  define Q:: "nat \<Rightarrow> bool" where 
"Q = (\<lambda> n. P (shiftEnv s n))"
  from Q_def  1 shift_toEnvNum have P_def:
"\<And> s'. toEnvP s' \<and> substate s' s \<Longrightarrow> 
P s' = Q (toEnvNum s' s)" by auto
  define n where "n = toEnvNum s2 s"
  assume base: "P s" and ind_step:
"\<And> s2. toEnvP s2 \<Longrightarrow>
 (substate s1 s2 \<and> substate s2 s \<and> s1 \<noteq> s2)
 \<Longrightarrow> P s2 \<Longrightarrow> P (predEnv s2)"
  show ?thesis
  proof
    assume 2: "toEnvP s2 \<and> substate s1 s2 \<and>
 substate s2 s"
    with 1  n_def shift_toEnvNum have s2_def: "s2 = shiftEnv s n"
      by auto
     from 2 show "P s2"
      apply(simp only: P_def n_def[symmetric])
      apply(simp only: s2_def)
    proof(induction n)
      case 0
      then show ?case using Q_def base by auto
    next
      case (Suc n)
      then show ?case 
        apply(simp only: Q_def)
        print_state
      proof cases
        assume 3: "s1 = shiftEnv s n"
        assume 4: "toEnvP (shiftEnv s (Suc n)) \<and>
    substate s1 (shiftEnv s (Suc n)) \<and>
    substate (shiftEnv s (Suc n)) s"
        from 3 have 5: "substate (shiftEnv s n) s1"
          using substate_refl by auto
        from predEnv_substate have
"substate (shiftEnv s (Suc n)) (shiftEnv s n)"
          by simp
        with 4 5  substate_asym substate_trans
        have "s1 = shiftEnv s (Suc n)"
          by blast
        with 3 have
 "shiftEnv s n = shiftEnv s (Suc n)" by simp
        with 4 gtime_predE show
 "P (shiftEnv s (Suc n))" by fastforce
      next
        assume 3: "s1 \<noteq> shiftEnv s n"
        assume 4: "toEnvP (shiftEnv s (Suc n)) \<and>
    substate s1 (shiftEnv s (Suc n)) \<and>
    substate (shiftEnv s (Suc n)) s"
from predEnv_substate have
"substate (shiftEnv s (Suc n)) (shiftEnv s n)"
  by simp
  with 4 substate_trans have 5:
 "substate s1 (shiftEnv s n)" by blast
  have 6: "toEnvP (shiftEnv s n)"
  proof cases
    assume "n = 0"
    with 1 show ?thesis by auto
  next 
    assume "n \<noteq> 0"
    with shiftEnvP_or_emptyState have
"toEnvP (shiftEnv s n) \<or>
 (shiftEnv s n) =  emptyState" by auto
    thus ?thesis
    proof
      assume "toEnvP (shiftEnv s n)"
      thus ?thesis by assumption
    next
      assume "shiftEnv s n = emptyState"
      with 5 have "s1 = emptyState"
        by (cases s1; auto)
      with 1 show ?thesis by auto
    qed
  qed
  assume "(toEnvP (shiftEnv s n) \<and>
     substate s1 (shiftEnv s n) \<and>
     substate (shiftEnv s n) s \<Longrightarrow>
     P (shiftEnv s n))"
  with 3 5 6 shiftEnv_substate ind_step
  show "P (shiftEnv s (Suc n))"
    by auto
qed
qed
qed
qed

lemma div_inverse:
"(s0::nat) = s1 div a \<and> s1 mod a = 0 \<Longrightarrow> s0 * a = s1"
  by auto

lemma toEnvNum_predEnv:
"toEnvP s \<and> toEnvP s' \<and> substate s s' \<and>toEnvNum s s' = 1\<Longrightarrow> s = predEnv s'"
  by (metis One_nat_def shiftEnv.simps(1) shiftEnv.simps(2) shift_toEnvNum)
 
lemma predEnv_toEnvNum:
"toEnvP s \<and> toEnvP s' \<and> substate s s' \<and>s = predEnv s' \<Longrightarrow> toEnvNum s s' = 1"
  by (metis add_diff_cancel_left' emptyState_substate gtime_predE toEnvNum3)

lemma toEnvP_emptyState:"\<not>toEnvP emptyState" by auto

lemma predEnv_det:"s1 = predEnv s \<and> s2 = predEnv s \<Longrightarrow> s1 = s2"
  by simp

lemma next_state_exists:
"toEnvP s0 \<and> toEnvP s1 \<and> substate s0 s1 \<and> s0 \<noteq>s1 \<Longrightarrow> \<exists>s2. toEnvP s2 \<and> substate s2 s1 \<and> s0 = predEnv s2"
  by sorry

lemma toEnvNum_leq:
"substate s1 s2 \<and> substate s2 s3 \<and> toEnvNum s1 s3 \<le>x \<Longrightarrow> 
  toEnvNum s1 s2 \<le> x \<and> toEnvNum s2 s3 \<le> x"
  by (metis add_leE toEnvNum3)

primrec timeContinuous:: "state \<Rightarrow> state \<Rightarrow> process \<Rightarrow> bool" where
"timeContinuous s emptyState p =
 (if s = emptyState then True else False)" 
| "timeContinuous s (toEnv s1) p =
  (if s = toEnv s1 then True else timeContinuous s s1 p)" 
| "timeContinuous s (setVarBool s1 v u) p = 
  (if s = setVarBool s1 v u then True else timeContinuous s s1 p)" 
| "timeContinuous s (setVarInt s1 v u) p =
  (if s = setVarInt s1 v u then True else timeContinuous s s1 p)"
| "timeContinuous s (setVarNat s1 v u) p =
  (if s = setVarNat s1 v u then True else timeContinuous s s1 p)"
| "timeContinuous s (setVarReal s1 v u) p =
  (if s = setVarReal s1 v u then True else timeContinuous s s1 p)"
| "timeContinuous s (setPstate s1 p1 q) p = 
  (if p = p1 then False else timeContinuous s s1 p)" 
| "timeContinuous s (reset s1 p1) p = 
  (if p = p1 then False else timeContinuous s s1 p)"

lemma timeContinuous_id:
"toEnvP s \<Longrightarrow> timeContinuous s s p"
  by (metis timeContinuous.simps(2) toEnvP.elims(2))

lemma substate_toEnvNum:
"substate s s' \<Longrightarrow> toEnvNum emptyState s \<le> toEnvNum emptyState s'"
  by (simp add: emptyState_substate toEnvNum3)


end