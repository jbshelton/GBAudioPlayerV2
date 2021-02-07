INCLUDE "hardware.inc"

SECTION "Timer interrupt", ROM0[$50]
TimerInterrupt:
    call method
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
	xor a
	ldh [rNR52], a
	nop
	nop
	nop
	nop
	ld a, $80
	ldh [rNR52], a
GBCmode:
	ld a, $01
	ldh [rKEY1], a
	stop
	nop
	nop
	nop
	nop
GBmode:
	xor a
	ldh [rIE], a
	ld sp, $FFFF
	ld hl, $4000
	ld bc, $0001
	ld e, $02
	xor a
	ldh [rNR10], a
StereoSetup:
	ld a, $12
	ldh [rNR51], a
	ld a, $8F
	ldh [rNR12], a
	ldh [rNR22], a
	xor a
	ldh [rNR13], a
	ldh [rNR23], a
MonoSetup:
	ld a, $11
	ldh [rNR51], a
	ld a, $8F
	ldh [rNR12], a
	xor a
	ldh [rNR13], a
finishSetup:
	ld a, $C0
	ldh [rNR11], a
	ldh [rNR21], a
	ld a, $80
	ldh [rNR14], a
	ldh [rNR24], a
	ld a, $77
	ldh [rNR50], a
	ld a, 000 ;timer divider
	ldh [rTMA], a
	ld a, %00000110 ;timer master
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
	jr z, sampleEndMonoGB
	ld h, $40
	inc bc
	ld a, c
	ld [$2000], a
	ld a, b
	ld [$3000], a
sampleEndMonoGB:
	ld sp, $FFFF
	ei
	jp lockup

StereoGB:
	ld a, [hli]
	ld d, a
	or $0F
	ldh [rNR12], a
	swap d
	ld a, d
	or $0F
	ldh [rNR22], a
	ld a, $80
	ldh [rNR14], a
	ldh [rNR24], a
	bit 7, h
	jr z, sampleEndStereoGB
	ld h, $40
	inc bc
	ld a, c
	ld [$2000], a
	ld a, b
	ld [$3000], a
sampleEndStereoGB:
	ld sp, $FFFF
	ei
	jp lockup

MonoHQ:
	ld a, [hli]
	ld d, a
	or $0F
	ldh [rNR12], a 
	ld a, d
	swap a
	and $F0
	ld d, a
	swap d
	or d
	ldh [rNR50], a
	bit 7, h
	jr z, sampleEndMonoHQ
	ld h, $40
	inc bc
	ld a, c
	ld [$2000], a
	ld a, b
	ld [$3000], a
sampleEndMonoHQ:
	ld sp, $FFFF
	ei
	jp lockup

StereoHQ:
	ld a, [hli]
	ld d, a
	or $0F
	ldh [rNR12], a 
	ld a, d
	swap a
	or $0F
	ldh [rNR22], a 
	ld a, $80
	ldh [rNR14], a 
	ldh [rNR24], a 
	ld a, [hli]
	ldh [rNR50], a
	bit 7, h
	jr z, sampleEndStereoHQ
	ld h, $40
	inc bc
	ld a, c
	ld [$2000], a
	ld a, b
	ld [$3000], a

sampleEndStereoHQ:
	ld sp, $FFFF
	ei
	jp lockup

lockup:
	nop
	nop
	nop
	nop
	jr lockup
SECTION "Additional lockup", ROM0[$0600]
	jp lockup