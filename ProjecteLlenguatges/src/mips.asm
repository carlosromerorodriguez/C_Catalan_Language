.data
.text
j main

FUNC_Factorial:
	sub $sp, $sp, 16
	sw $fp, 0($sp)
	sw $ra, 4($sp)
	sw $a0, 8($sp)
	move $fp, $sp
	li $t0, 0
	L0:
	li $t2, 0
	seq $t1, $a0, $t2
	xori $t2, $t1, 0x1
	bne $t2, $zero, L1
	li $t0, 1
	j L3
	L1:
	L2:
	addi $t2, $a0, -1
	move $a0, $t2
	sw $t0, 12($fp)
	jal FUNC_Factorial
	move $t0, $v0
	
	lw $a0, 8($fp)
	mul $t2, $a0, $t0
	lw $t0, 12($fp)
	move $t0, $t2
	L3:
	move $sp, $fp
	lw $fp, 0($sp)
	lw $ra, 4($sp)
	addi $sp, $sp, 8
	move $v0, $t0
	jr $ra
# END_FUNC

main:
	sub $sp, $sp, 4
	move $fp, $sp
	li $t0, 5
	move $a0, $t0
	jal FUNC_Factorial
	move $t0, $v0
	
	move $t1, $t0
	
	li $v0, 10
	syscall # Finalitzem el programa
	addi $sp, $sp, 4
