export const BOOKING_CREATED_NOTIFICATIONS = {
  title: {
    en: 'New booking from %name',
    ru: 'Новое бронирование от %name',
  },
  body: {
    en: '%name booked on %date',
    ru: '%name забронировал(а) на %date',
  },
};

export const CANCEL_BOOKING_NOTIFICATIONS = {
  title: {
    en: 'The booking has been canceled',
    ru: 'Отмена бронирования',
  },
  body: {
    en: '%name has canceled booking on %date',
    ru: '%name отменил(а) бронирование за %date',
  },
};

export const REBOOKING_NOTIFICATIONS = {
  title: {
    en: 'Rebooking from %name',
    ru: 'Переброниевание от %name',
  },
  body: {
    en: '%name has rebooked to %date',
    ru: '%name перебронировал(а) на %date',
  },
};

export const USER_ADDED_TO_BRANCH_NOTIFICATIONS = {
  title: {
    en: 'You were added to the branch',
    ru: 'Вас добавили в филиал',
  },
  body: {
    en: '%name added you to the branch at the address "%address"',
    ru: '%name добавил(а) вас в филиал по адресу "%address"',
  },
};

export const USER_REMOVED_FROM_BRANCH_NOTIFICATIONS = {
  title: {
    en: 'You were removed from the branch',
    ru: 'Вас удалили из филиала',
  },
  body: {
    en: '%name removed you from the branch at the address "%address"',
    ru: '%name удалил(а) вас из филилала по адресу "%address"',
  },
};

export const FINISH_BOOKING_NOTIFICATIONS = {
  title: {
    en: 'Finish of booking',
    ru: 'Завершение бронирования',
  },
  body: {
    en: '%name has finished the booking on %date',
    ru: '%name завершил(а) бронирование за %date',
  },
};

export const BROADCAST_CANCEL_NOTIFICATIONS = {
  title: {
    en: '%name canceled booking for %date',
    ru: '%name отменил(а) бронирование за %date',
  },
};

export const PAYMENT_DATE_APPROACH_NOTIFICATION = {
  title: {
    en: 'Payment date approaches',
    ru: 'Приближается день оплаты',
  },
  body: {
    en: 'Your payment date will be on %date, please make sure you have enough funds on the balance to continue using service',
    ru: 'День оплаты будет %date, убедитесь что у вас достаточно средств на балансе чтобы использовать сервис',
  },
};

export const SERVICE_BLOCKED_NOTIFICATIONS = {
  title: {
    en: 'Service blocked',
    ru: 'Сервис заблокирован',
  },
  body: {
    en: 'Your service has been blocked due to insufficient funds on the balance. Fill in the balance to continue using your service',
    ru: 'Ваш сервис был заблокирован из-зи нехватки средств на балансе. Пополните баланс чтобы продолжить пользование',
  },
};

export const TRIAL_PERIOD_APPROACHES_NOTIFICATION = {
  title: {
    en: 'Trial period is about to end',
    ru: 'Окончание пробного периода',
  },
  body: {
    en: 'Your trial period ends on %date',
    ru: 'Ваш пробный период заканчивается %date',
  },
};

export const BALANCE_FEE_CHARGED_NOTIFICATION = {
  title: {
    en: 'Fee charge',
    ru: 'Списание с баланса',
  },
  body: {
    en: 'A monthly fee of %fee has been charged from your balance',
    ru: 'С вашего баланса была списана ежемесячная оплата в размере %fee',
  },
};
