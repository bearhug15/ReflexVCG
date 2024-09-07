theory FridgeTheory
imports Reflex
begin

fun ltime:: "state \<Rightarrow> process \<Rightarrow> nat" where 
"ltime emptyState _ = 0" 
| "ltime (toEnv s) p = (ltime s p) + 100" 
| "ltime (setVarBool s _ _) p = ltime s p" 
| "ltime (setVarInt s _ _) p = ltime s p"
| "ltime (setVarNat s _ _) p = ltime s p"
| "ltime (setVarReal s _ _) p = ltime s p"
| "ltime (setPstate s p1 _) p =
  (if p=p1 then 0 else ltime s p)" 
| "ltime (reset s p1) p =
  (if p=p1 then 0 else ltime s p)"

lemma ltime_mod:
assumes "ltime s0 p < a*100"
shows "ltime s0 p \<le> (a*100-100)"
proof -
have"(ltime s0 p) mod 100 = 0" by (induction s0) (auto)
thus ?thesis using assms by (induction a) (auto)
qed
lemma ltime_mult:
"ltime s p mod 100 = 0"
  by (induction s) (auto)

lemma ltime_mod:
assumes "ltime s0 p < a*100"
shows "ltime s0 p ≤ (a*100-100)"
proof -
have"(ltime s0 p) mod 100 = 0" by (induction s0) (auto)
thus ?thesis using assms by (induction a) (auto)
qed

lemma ltime_div_less:
assumes "(ltime s0 p div 100)≤ a"
shows "(ltime s0 p -100) div 100 < a ∨ ltime s0 p = 0"
proof -
have"(ltime s0 p) mod 100 = 0" by (induction s0) (auto)
thus ?thesis using assms by (induction a) (auto)
qed

lemma ltime_le_toEnvNum: 
"ltime s p div 100 ≤ toEnvNum emptyState s"
  apply(induction s)
         apply(auto)
  done

<<<<<<< HEAD
(*
definition R1 where "R1 s ≡ toEnvP s ∧
(∀ s1 s2. substate s1 s2 ∧ substate s2 s ∧ toEnvP s1 ∧ toEnvP s2 ∧ toEnvNum s1 s2 = 1 ∧
 getVarBool s1 fridgeDoor' = CLOSED' ∧ getVarBool s2 fridgeDoor' = OPEN' ⟶ getVarBool s2 lighting')"

definition R2 where "R2 s ≡ toEnvP s ∧
(∀ s1 s2. substate s1 s2 ∧ substate s2 s ∧ toEnvP s1 ∧ toEnvP s2 ∧ toEnvNum s1 s2 = 1 ∧
 getVarBool s1 fridgeDoor' = OPEN' ∧ getVarBool s2 fridgeDoor' = CLOSED' ⟶ ¬ getVarBool s2 lighting')"

definition R3 where "R3 s ≡ toEnvP s ∧
(∀ s1. substate s1 s ∧ toEnvP s1 ∧ toEnvNum s1 s ≥ OPEN_DOOR_TIME_LIMIT'TIMEOUT ∧  getVarBool s1 fridgeDoor' = OPEN' ⟶
(∃ s3. toEnvP s3 ∧ substate s1 s3 ∧ substate s3 s ∧ toEnvNum s1 s3 ≤ OPEN_DOOR_TIME_LIMIT'TIMEOUT ∧
(getVarBool s3 doorSignal' ∨ getVarBool s3 fridgeDoor' = CLOSED') ∧
(∀ s2. toEnvP s2 ∧ substate s1 s2 ∧ substate s2 s3 ∧ s2 ≠ s3 ⟶
 ¬ getVarBool s2 doorSignal' ∧ getVarBool s2 fridgeDoor' = OPEN')))"

definition R4 where "R4 s ≡ toEnvP s ∧
(∀ s1 s2 s3. substate s1 s2 ∧ substate s2 s3 ∧ substate s3 s ∧ toEnvP s1 ∧ toEnvP s2 ∧
 toEnvP s3 ∧ toEnvNum s1 s2 = 1 ∧ toEnvNum s2 s3 < OPEN_DOOR_TIME_LIMIT'TIMEOUT ∧
getVarBool s1 fridgeDoor' = CLOSED' ∧ getVarBool s2 fridgeDoor' = OPEN' ⟶
¬ getVarBool s3 doorSignal')"

definition R5 where "R5 s ≡ toEnvP s ∧
(∀ s1 s2. substate s1 s2 ∧ substate s2 s ∧ toEnvP s1 ∧ toEnvP s2 ∧ toEnvNum s1 s2 = 1 ∧ 
getVarBool s1 fridgeCompressor' = False ∧ getVarBool s2 fridgeTempGreaterMax' ⟶ getVarBool s2 fridgeCompressor' = True)"

*)

(*
P2_1 1. При открытии двери холодильника включается освещение.
P2_1 2. При закрытии двери холодильника выключается освещение.
(нет) 3. Если дверь холодильника открыта, то не более, чем через OPEN_DOOR_TIME_LIMIT миллисекунд, подается сигнал, если за это время пользователь не закроет дверь.
P3 4. Если дверь холодильника только что открыта, сигнал будет подан не менее, чем через OPEN_DOOR_TIME_LIMIT миллисекунд.
P2_1 5. Если температура в холодильнике превышает максимальную, включается  компрессор.*)

definition R3 where 
"R3 s ≡ toEnvP s ∧
(∀ s1. substate s1 s ∧ toEnvP s1 ∧ 
  toEnvNum s1 s ≥ 300 div 100 ∧  
  getVarBool s1 ''fridgeDoor'' = True ⟶
    (∃ s3. toEnvP s3 ∧ substate s1 s3 ∧ substate s3 s ∧ 
      toEnvNum s1 s3 ≤ 300 div 100 ∧
      (getVarBool s3 ''doorSignal'' ∨ 
        getVarBool s3 ''fridgeDoor'' = False) ∧
      (∀ s2. toEnvP s2 ∧ substate s1 s2 ∧ 
        substate s2 s3 ∧ s2 ≠ s3 ⟶
          ¬ getVarBool s2 ''doorSignal'' ∧ 
          getVarBool s2 ''fridgeDoor'' = True)))"


=======
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
end
