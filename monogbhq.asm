INCLUDE "hardware.inc"

SECTION "Timer interrupt", ROM0[$50]
TimerInterrupt:
    nop
    jp MonoGBHQ
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
    xor a
    ldh [rIE], a
    ld e, $0F
    ld hl, $4000
    ld bc, $0001
    xor a
    ldh [rNR10], a
    ld a, $11
    ldh [rNR51], a
    ld a, $E0
    ldh [rNR13], a
    ld a, $f0
    ldh [rNR12], a
    ld a, $c0
    ldh [rNR11], a
    ld a, $83
    ldh [rNR14], a
    ld a, $44
    ldh [rNR50], a
    ld a, 000 ;timer divider
    ldh [rTMA], a
    ld a, %00000101
    ldh [rTAC], a
    ld a, $04
    ldh [rIE], a
    ei

waitforint:
    nop
    nop
    nop
    jr waitforint

MonoGBHQ:
    ld a, [hli]
	ld d, a
	or e
	ldh [rNR12], a 
    ld a, $80
    ldh [rNR14], a
	ld a, d
	swap a
	and $F0
	ld d, a
	swap d
	or d
	ldh [rNR50], a
	bit 7, h
	jr z, sampleEnd
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