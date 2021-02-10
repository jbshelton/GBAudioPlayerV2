INCLUDE "hardware.inc"

SECTION "Timer interrupt", ROM0[$50]
TimerInterrupt:
    jp MonoGB
	nop
	nop
	nop
	nop
	nop

SECTION "Header", ROM0[$100]

EntryPoint:
	di
	jp Start

REPT $150 - $104
	db 0
ENDR

SECTION "Game code", ROM0[$150]

Start:
	di
    nop
    nop
    xor a
    ldh [rNR52], a
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    ld a, $80
    ldh [rNR52], a
    nop
    nop
    nop
	ld a, $01
	ldh [rKEY1], a
	stop
	nop
	nop
	nop
	nop
	xor a
	ldh [rIE], a
	ld hl, $4000
	ld bc, $0001
	ld e, $02
	xor a
	ldh [rNR10], a
	ld a, $11
	ldh [rNR51], a
	ld a, $F0
	ldh [rNR12], a
	ld a, $80
	ldh [rNR13], a
	ld a, $C0
	ldh [rNR11], a
	ld a, $87
	ldh [rNR14], a
	ld a, $77
	ldh [rNR50], a
	ld a, 000 ;timer divider
	ldh [rTMA], a
	ld a, %00000101
	ldh [rTAC], a
	ld a, $04
	ldh [rIE], a
	ei

waitforInt:
	nop
	nop
	nop
	jr waitforInt

MonoGB:
	dec e
	jr nz, noResetM
	ld e, $02
	ld a, [hli]
	ld d, a
noResetM:
	ld a, d
	or $0F
	ldh [rNR12], a
	swap a
	ld d, a
	ld a, $80
	ldh [rNR14], a
	bit 7, h
	jr z, sampleEnd:
	ld h, $40
	inc bc
	ld a, c
	ld [$2000], a
	ld a, b
	ld [$3000], a
sampleEnd:
	reti

lockup:
	nop
	nop
	nop
	nop
	jr lockup
SECTION "Additional lockup", ROM0[$0600]
	jp lockup