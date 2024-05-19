j main

FUNC_Mul:
	sub $sp, $sp, 20
	sw $fp, 0($sp)
	sw $ra, 4($sp)
	sw $a0, 8($sp)
	sw $a1, 12($sp)
	move $fp, $sp
	li $t0, 0
	L0:
	li $t2, 0
	seq $t1, $a1, $t2
	xori $t1, $t1, 0x1
	bne $t1, $zero, L1
	li $t0, 0
	j L3
	L1:
	L2:
	move $a0, $a0
	addi $t1, $a1, -1
	move $a1, $t1
	sw $t0, 16($fp)
	jal FUNC_Mul
	move $t0, $v0
	
	lw $a0, 8($fp)
	add $t1, $t0, $a0
	lw $t0, 16($fp)
	move $t0, $t1
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
	li $t0, 40
	move $a0, $t0
	li $t0, 92
	move $a1, $t0
	jal FUNC_Mul
	move $t0, $v0
	
	move $t1, $t0
	
	li $v0, 10
	syscall # Finalitzem el programa
	addi $sp, $sp, 4
