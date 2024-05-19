.data
	$msg1: .asciiz "Fibonacci de: "
	$msg2: .asciiz "\n"
	$msg3: .asciiz " = "
.text
j main

FUNC_Fibonacci:
	sub $sp, $sp, 20
	sw $fp, 0($sp)
	sw $ra, 4($sp)
	sw $a0, 8($sp)
	move $fp, $sp
	li $t0, 0
	L0:
	li $t2, 2
	slt $t1, $a0, $t2
	xori $t1, $t1, 0x1
	bne $t1, $zero, L1
	move $t0, $a0
	j L3
	L1:
	L2:
	addi $t1, $a0, -1
	move $a0, $t1
	sw $t0, 12($fp)
	jal FUNC_Fibonacci
	move $t0, $v0
	
	lw $a0, 8($fp)
	addi $t1, $a0, -2
	move $a0, $t1
	sw $t0, 16($fp)
	jal FUNC_Fibonacci
	move $t0, $v0
	
	lw $t1, 16($fp)
	add $t3, $t1, $t0
	lw $t0, 12($fp)
	move $t0, $t3
	L3:
	move $sp, $fp
	lw $fp, 0($sp)
	lw $ra, 4($sp)
	addi $sp, $sp, 8
	move $v0, $t0
	jr $ra
# END_FUNC

main:
	sub $sp, $sp, 12
	move $fp, $sp
	li $t0, 0
	li $t1, 0
	li $t2, 0
	li $t0, 0
	LOOP4:
	li $t3, 10
	slt $t2, $t0, $t3
	xori $t4, $t2, 0x1
	bne $t4, $zero, L5
	addi $t0, $t0, 1
	move $a0, $t0
	sw $t2, 8($fp)
	sw $t1, 4($fp)
	sw $t0, 0($fp)
	jal FUNC_Fibonacci
	move $t1, $v0
	
	lw $t2, 4($fp)
	move $t2, $t1
	li $v0, 4
	la $a0, $msg1
	syscall
	
	lw $t0, 0($fp)
	li $v0, 1
	move $a0, $t0
	syscall
	
	li $v0, 4
	la $a0, $msg3
	syscall
	
	li $v0, 1
	move $a0, $t2
	syscall
	
	li $v0, 4
	la $a0, $msg2
	syscall
	
	j LOOP4
	L5:
	
	li $v0, 10
	syscall # Finalitzem el programa
	addi $sp, $sp, 12
