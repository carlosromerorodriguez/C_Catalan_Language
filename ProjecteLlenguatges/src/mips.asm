j main

FUNC_sumUp:
	sub $sp, $sp, 16
	sw $fp, 0($sp)
	sw $ra, 4($sp)
	sw $a0, 8($sp)
	move $fp, $sp
	L0:
	li $t1, 0
	seq $t0, $a0, $t1
	xori $t0, $t0, 0x1
	bne $t0, $zero, L1
	li $t0, 0
	j L3
	L1:
	L2:
	addi $t2, $a0, -1
	move $a0, $t2
	sw $t0, 12($fp)
	jal FUNC_sumUp
	move $t0, $v0
	
	lw $a0, 8($fp)
	add $t2, $a0, $t0
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
	li $t0, 200
	move $a0, $t0
	jal FUNC_sumUp
	move $t0, $v0
	
	move $t1, $t0
	
	li $v0, 10
	syscall # Finalitzem el programa
	addi $sp, $sp, 4
