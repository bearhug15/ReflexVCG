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

fun getVarBool:: "state \<Rightarrow> variable \<Rightarrow> bool" where
"getVarBool emptyState _ = False"
| "getVarBool (toEnv s) x = getVarBool s x" 
| "getVarBool (setVarBool s y v) x =
  (if x=y then v else (getVarBool s x))" 
| "getVarBool (setVarInt s _ _) x = getVarBool s x" 
| "getVarBool (setVarNat s _ _) x = getVarBool s x"
| "getVarBool (setVarReal s _ _) x = getVarBool s x" 
| "getVarBool (setPstate s _ _) x = getVarBool s x" 
| "getVarBool (reset s _) x = getVarBool s x" 

fun getVarInt:: "state \<Rightarrow> variable \<Rightarrow> int" where
"getVarInt emptyState _ = 0"
| "getVarInt (toEnv s) x = getVarInt s x" 
| "getVarInt (setVarBool s y v) x = getVarInt s x" 
| "getVarInt (setVarInt s y v) x = 
  (if x=y then v else (getVarInt s x))"
| "getVarInt (setVarNat s _ _) x = getVarInt s x"
| "getVarInt (setVarReal s _ _) x = getVarInt s x"
| "getVarInt (setPstate s _ _) x = getVarInt s x" 
| "getVarInt (reset s _) x = getVarInt s x"

fun getVarNat:: "state \<Rightarrow> variable \<Rightarrow>nat" where
"getVarNat emptyState _ = 0"
| "getVarNat (toEnv s) x = getVarNat s x"
| "getVarNat (setVarBool s _ _) x = getVarNat s x"
| "getVarNat (setVarInt s _ _) x = getVarNat s x"
| "getVarNat (setVarNat s y v) x = 
  (if x=y then v else getVarNat s x)"
| "getVarNat (setVarReal s _ _) x = getVarNat s x"
| "getVarNat (setPstate s _ _) x = getVarNat s x"
| "getVarNat (reset s _) x = getVarNat s x"

fun getVarReal:: "state \<Rightarrow> variable \<Rightarrow>real" where
"getVarReal emptyState _ = 0.0"
| "getVarReal (toEnv s) x = getVarReal s x"
| "getVarReal (setVarBool s _ _) x = getVarReal s x"
| "getVarReal (setVarInt s _ _) x = getVarReal s x"
| "getVarReal (setVarNat s y v) x = getVarReal s x"
| "getVarReal (setVarReal s y v) x = (if x=y then v else getVarReal s x)"
| "getVarReal (setPstate s _ _) x = getVarReal s x"
| "getVarReal (reset s _) x = getVarReal s x"

fun getPstate:: "state \<Rightarrow> process \<Rightarrow> pstate" where
"getPstate emptyState _ = ''''"
| "getPstate (toEnv s) p = getPstate s p" 
| "getPstate (setVarBool s _ _) p = getPstate s p" 
| "getPstate (setVarInt s _ _) p = getPstate s p"
| "getPstate (setVarNat s _ _) p = getPstate s p"
| "getPstate (setVarReal s _ _) p = getPstate s p"
| "getPstate (setPstate s p1 q) p =
  (if p=p1 then q else (getPstate s p))" 
| "getPstate (reset s _) p = getPstate s p"

fun substate:: "state \<Rightarrow> state \<Rightarrow> bool" where
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

fun toEnvNum:: "state \<Rightarrow>state \<Rightarrow> nat" where 
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

fun toEnvP::"state \<Rightarrow> bool" where
"toEnvP (toEnv _) = True" |
"toEnvP _ = False"

fun ltime:: "state \<Rightarrow> process \<Rightarrow> nat" where 
"ltime emptyState _ = 0" 
| "ltime (toEnv s) p = (ltime s p) + 1" 
| "ltime (setVarBool s _ _) p = ltime s p" 
| "ltime (setVarInt s _ _) p = ltime s p"
| "ltime (setVarNat s _ _) p = ltime s p"
| "ltime (setVarReal s _ _) p = ltime s p"
| "ltime (setPstate s p1 _) p =
  (if p=p1 then 0 else ltime s p)" 
| "ltime (reset s p1) p =
  (if p=p1 then 0 else ltime s p)"

fun predEnv:: "state \<Rightarrow> state" where
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

fun shiftEnv:: "state \<Rightarrow> nat \<Rightarrow> state" where
"shiftEnv s 0 = s" |
"shiftEnv s (Suc n) = predEnv (shiftEnv s n)"


(*
definition R1 where
"R1 s \<equiv>
toEnvP s \<and>
(\<forall> s1 s2.  substate s1 s2 \<and>
 substate s2 s \<and> toEnvP s1 \<and> toEnvP s2 \<and>
 toEnvNum s1 s2 = 1 \<and>
getVarBool s1 ''_in_1'' = False \<and>
 getVarBool s1 ''_out_1'' = False \<and>
getVarBool s2 ''_in_1'' = True \<longrightarrow>
(\<exists> s4. toEnvP s4 \<and> substate s2 s4 \<and> substate s4 s\<and>
toEnvNum s2 s4 \<le> 1 \<and> getVarBool s4 ''_out_1'' = True \<and>
(\<forall> s3. toEnvP s3 \<and> substate s2 s3 \<and>
 substate s3 s4 \<and> s3 \<noteq> s4 \<longrightarrow>
 getVarBool s3 ''_in_1'' = True)))"
declare R1_def[simp]

definition extraInv where
"extraInv s \<equiv>
toEnvP s \<and>
(getPstate s ''Dryer'' = ''Work'' \<longrightarrow>
0 < ltime s ''Dryer'' \<and> ltime s ''Ctrl'' \<le> 2000 \<and>
(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and>
 toEnvNum s1 s + 1 = ltime s ''Ctrl'' \<longrightarrow>
getVarBool s1 ''_in_1'' = True \<and>
 getVarBool s1 ''_out_1'' = True) \<and>
(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and>
 toEnvNum s1 s + 1 < ltime s ''Ctrl'' \<longrightarrow>
getVarBool s1 ''_in_1'' = False \<and>
 getVarBool s1 ''_out_1'' = True)) \<and>
(\<forall> s1. toEnvP s1 \<and> substate s1 s \<longrightarrow>
getPstate s1 ''Dryer'' = ''Wait'' \<or>
getPstate s1 ''Dryer'' = ''Work'')"
declare extraInv_def[simp]

definition inv where "inv s \<equiv> R1 s \<and> extraInv s"
declare inv_def[simp]
*)

end