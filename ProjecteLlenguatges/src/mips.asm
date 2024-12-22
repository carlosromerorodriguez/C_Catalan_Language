.data
	$msg1: .asciiz "El valor de Fibonacci de "
	$msg2: .asciiz " es "
.text
j main

FUNC_Fibonacci:
	sub $sp, $sp, 20
	sw $fp, 0($sp)
	sw $ra, 4($sp)
	sw $a0, 8($sp)
	move $fp, $sp
	L0:
	li $t1, 2
	slt $t0, $a0, $t1
	xori $t0, $t0, 0x1
	bne $t0, $zero, L1
	move $t0, $a0
	j L3
	L1:
	L2:
	addi $t2, $a0, -1
	move $a0, $t2
	sw $t0, 12($fp)
	jal FUNC_Fibonacci
	move $t0, $v0
	
	lw $a0, 8($fp)
	addi $t2, $a0, -2
	move $a0, $t2
	sw $t0, 16($fp)
	jal FUNC_Fibonacci
	move $t0, $v0
	
	lw $t2, 16($fp)
	add $t3, $t2, $t0
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
	sub $sp, $sp, 8
	move $fp, $sp
	li $t0, 12
	move $a0, $t0
	sw $t0, 0($fp)
	jal FUNC_Fibonacci
	move $t0, $v0
	
	move $t1, $t0
	li $v0, 4
	la $a0, $msg1
	syscall
	
	lw $t0, 0($fp)
	li $v0, 1
	move $a0, $t0
	syscall
	
	li $v0, 4
	la $a0, $msg2
	syscall
	
	li $v0, 1
	move $a0, $t1
	syscall
	
	
	li $v0, 10
	syscall # Finalitzem el programa
	addi $sp, $sp, 8
