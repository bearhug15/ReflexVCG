theory "ReflexBase"
  imports Main HOL.Real
begin

type_synonym variable = string

type_synonym process = string

type_synonym pstate = string

type_synonym field = string

type_synonym index = nat

datatype val =
    ValBool bool
  | ValInt int
  | ValNat nat
  | ValReal real
  | ValStruct "field \<Rightarrow> val"
  | ValArray "index \<Rightarrow> val"
  | Nil

datatype access = 
  AccessField field
  | AccessIndex index 

datatype state =
    emptyState
  | setVar state variable val
  | toEnv state 
  | setPstate state process pstate 
  | reset state process



primrec defaultVal :: "val \<Rightarrow> val" where 
  "defaultVal (ValBool _) = ValBool False" 
| "defaultVal (ValInt _) = ValInt 0" 
| "defaultVal (ValNat _) = ValNat 0" 
| "defaultVal (ValReal _) = ValReal 0.0" 
| "defaultVal (ValStruct _) = ValStruct (\<lambda>_. Nil)" 
| "defaultVal (ValArray _) = ValArray (\<lambda>_. Nil)"
| "defaultVal Nil = Nil"

fun applyAccess :: "val \<Rightarrow> access list \<Rightarrow> val" where
  "applyAccess v [] = v"
| "applyAccess (ValStruct f) (AccessField fld # rest) = 
    applyAccess (f fld) rest"
| "applyAccess (ValArray a) (AccessIndex i # rest) =
    applyAccess (a i) rest"
| "applyAccess v _ = defaultVal v"

primrec getVarVal :: "state \<Rightarrow> variable \<Rightarrow> access list \<Rightarrow> val" where
  "getVarVal emptyState _ _ = Nil"
| "getVarVal (setVar s v val) var accs = 
    (if v = var then applyAccess val accs
      else getVarVal s var accs)"
| "getVarVal (toEnv s) var accs = getVarVal s var accs"
| "getVarVal (setPstate s _ _) var accs = getVarVal s var accs"
| "getVarVal (reset s _) var accs = getVarVal s var accs"

fun updValPath :: "val \<Rightarrow> access list \<Rightarrow> val \<Rightarrow> val" where
  "updValPath v [] newVal = newVal"  
| "updValPath (ValStruct f) (AccessField fld # rest) newVal =
     ValStruct (\<lambda>fld'. if fld' = fld then updValPath (f fld) rest newVal
                else f fld')" 
| "updValPath (ValArray a) (AccessIndex i # rest) newVal =
     ValArray (\<lambda>j. if j = i then updValPath (a i) rest newVal
                else a j)" 
| "updValPath _ (_ # _) newVal = newVal"

definition setVarVal :: "state \<Rightarrow> variable \<Rightarrow> access list \<Rightarrow> val \<Rightarrow> state" where
  "setVarVal s v acc newVal =
     setVar s v (updValPath (getVarVal s v []) acc newVal)"


lemma simple_get_set:
  "getVarVal (setVarVal s var [] val) var [] = val"
  by (simp add: setVarVal_def )

fun valid_access_path :: "val \<Rightarrow> access list \<Rightarrow> bool" where
  "valid_access_path _ [] = True" |
  "valid_access_path (ValStruct f) (AccessField fld # rest) = valid_access_path (f fld) rest" |
  "valid_access_path (ValArray a) (AccessIndex i # rest) = valid_access_path (a i) rest" |
  "valid_access_path _ (_ # _) = False"

lemma apply_update_ident:
  assumes "valid_access_path v acc"
  shows   "applyAccess (updValPath v acc val) acc = val"
  using assms
  apply (induction acc arbitrary: v)
   apply simp
  using valid_access_path.elims(2) by force

lemma get_after_set:
  assumes "valid_access_path (getVarVal s var []) acc"
  shows   "getVarVal (setVarVal s var acc val) var acc = val"
  unfolding setVarVal_def
  apply simp
  apply (rule apply_update_ident)
  apply (rule assms)
  done


primrec getPstate:: "state \<Rightarrow> process \<Rightarrow> pstate" where
"getPstate emptyState _ = ''stop''"
| "getPstate (toEnv s) p = getPstate s p" 
| "getPstate (setVar s _ _) p = getPstate s p" 
| "getPstate (setPstate s p1 q) p =
  (if p=p1 then q else (getPstate s p))" 
| "getPstate (reset s _) p = getPstate s p"

primrec substate:: "state \<Rightarrow> state \<Rightarrow> bool" where
"substate s emptyState =
 (if s = emptyState then True else False)" 
| "substate s (toEnv s1) =
  (if s = toEnv s1 then True else substate s s1)" 
| "substate s (setVar s1 v u) = 
  (if s = setVar s1 v u then True else substate s s1)" 
| "substate s (setPstate s1 p q) =
  (if s = setPstate s1 p q then True else substate s s1)" 
| "substate s (reset s1 p) =
  (if s = reset s1 p then True else substate s s1)"

primrec toEnvNum:: "state \<Rightarrow>state \<Rightarrow> nat" where 
"toEnvNum s emptyState = 0" 
| "toEnvNum s (toEnv s1) = 
  (if s = toEnv s1 then 0 else toEnvNum s s1 + 1)" 
| "toEnvNum s (setVar s1 v u) =
  (if s = setVar s1 v u then 0 else toEnvNum s s1)" 
| "toEnvNum s (setPstate s1 p q) = 
  (if s = setPstate s1 p q then 0 else toEnvNum s s1)" 
| "toEnvNum s (reset s1 p) =
  (if s = reset s1 p then 0 else toEnvNum s s1)"

primrec toEnvP::"state \<Rightarrow> bool" where
"toEnvP (toEnv _) = True" 
| "toEnvP emptyState = False"
| "toEnvP (setVar _ _ _) = False"
| "toEnvP (setPstate _ _ _) = False"
| "toEnvP (reset _ _ ) = False"

(*
primrec toEnvP::"state \<Rightarrow> bool" where
"toEnvP (toEnv _) = True" 
| "toEnvP _ = False"
*)

primrec predEnv:: "state \<Rightarrow> state" where
"predEnv emptyState = emptyState" 
| "predEnv (toEnv s) =
  (if (toEnvP s) then s else predEnv s)" 
| "predEnv (setVar s _ _) = 
  (if (toEnvP s) then s else predEnv s)" 
| "predEnv (setPstate s _ _) = 
  (if (toEnvP s) then s else predEnv s)" 
| "predEnv (reset s _) = 
  (if (toEnvP s) then s else predEnv s)"

primrec shiftEnv:: "state \<Rightarrow> nat \<Rightarrow> state" where
"shiftEnv s 0 = s" |
"shiftEnv s (Suc n) = predEnv (shiftEnv s n)"


end