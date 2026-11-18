const genders = ['MALE', 'FEMALE', 'OTHER']

export function validatePassenger(passenger, seat) {
  const errors = {}
  const name = passenger.name.trim()
  const age = Number(passenger.age)

  if (name.length < 2 || name.length > 100) errors.name = 'Name must be between 2 and 100 characters.'
  if (!Number.isInteger(age) || age < 1 || age > 100) errors.age = 'Age must be a whole number from 1 to 100.'
  if (!genders.includes(passenger.gender)) errors.gender = 'Choose a passenger gender.'
  if (seat.ladiesOnly && passenger.gender !== 'FEMALE') errors.gender = 'This seat requires a FEMALE passenger.'
  return errors
}
