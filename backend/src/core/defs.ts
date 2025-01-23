export const USER_ROLES = {
  admin: '529b2630-8905-4f47-a46c-6b58f9aef84a',
  user: '529b2630-8905-4f47-a46c-6b58f9aef84b',
  provider: '529b2630-8905-4f47-a46c-6b58f9aef84c',
  helper: '529b2630-8905-4f47-a46c-6b58f9aef84d',
};

export const WS_MESSAGES = {
  SERVER: {
    WHO_ARE_YOU: 'WHO_ARE_YOU',
    ROLE_CHANGED: 'ROLE_CHANGED',
    MESSAGE_READ: 'SERVER_MESSAGE_READ',
    USER_OFFLINE: 'USER_OFFLINE',
    USER_ONLINE_STATUS: 'USER_ONLINE_STATUS',
    USER_ONLINE: 'USER_ONLINE',
    NEW_MESSAGE: 'NEW_MESSAGE',
    MESSAGE_DELETED: 'MESSAGE_DELETED',
    CHAT_REMOVED: 'CHAT_REMOVED',
    MESSAGE_EDITED: 'MESSAGE_EDITED',
    LOGGED_OUT: 'LOGGED_OUT',
  },
  CLIENT: {
    I_AM: 'I_AM',
    MESSAGE_READ: 'CLIENT_MESSAGE_READ',
    CHECK_USER_ONLINE: 'CHECK_USER_ONLINE',
    LOGOUT: 'LOGOUT',
  },
};

export const REDIS_NAMESPACE = {
  WS: 'ws:',
};

export const EVENTS = {
  CHANGE_ROLE: 'CHANGE_ROLE',
  MESSAGE_READ: 'MESSAGE_READ',
  HANDLE_USER_OFFLINE: 'HANDLE_USER_OFFLINE',
  CHECK_USER_ONLINE: 'CHECK_USER_ONLINE',
  HANDLE_USER_ONLINE: 'HANDLE_USER_ONLINE',
  USER_LOGGED_OUT: 'USER_LOGGED_OUT',
};

export const USER_RELATIONS = ['role'];

export const NOTIFICATION_RELATIONS = [
  'user',
  ...USER_RELATIONS.map((v) => 'user.' + v),
];

export const SERVICE_RELATIONS = [
  'user',
  'category',
  ...USER_RELATIONS.map((v) => 'user.' + v),
];

export const BRANCH_USERS_RELATIONS = [
  'user',
  ...USER_RELATIONS.map((v) => 'user.' + v),
];

export const BRANCH_RELATIONS = [
  'service',
  'users',
  ...SERVICE_RELATIONS.map((v) => 'service.' + v),
  ...BRANCH_USERS_RELATIONS.map((v) => 'users.' + v),
];

export const BOOKING_RELATIONS = [
  'service',
  'user',
  'branch',
  ...SERVICE_RELATIONS.map((v) => 'service.' + v),
  ...USER_RELATIONS.map((v) => 'user.' + v),
  ...BRANCH_RELATIONS.map((v) => 'branch.' + v),
];

export const FAVORITE_RELATIONS = [
  'user',
  'service',
  ...USER_RELATIONS.map((v) => 'user.' + v),
  ...SERVICE_RELATIONS.map((v) => 'service.' + v),
];

export const GALLERY_RELATIONS = [
  'service',
  'branch',
  ...SERVICE_RELATIONS.map((v) => 'service.' + v),
  ...BRANCH_RELATIONS.map((v) => 'branch.' + v),
];

export const CHAT_RELATIONS = [
  'user1',
  'user2',
  ...USER_RELATIONS.map((v) => 'user1.' + v),
  ...USER_RELATIONS.map((v) => 'user2.' + v),
];

const LOCALE_MESSAGE_RELATIONS = [
  'from',
  'to',
  'parent',
  'chat',
  'forwardedFrom',
  ...USER_RELATIONS.map((v) => 'from.' + v),
  ...USER_RELATIONS.map((v) => 'to.' + v),
  ...CHAT_RELATIONS.map((v) => 'chat.' + v),
  ...USER_RELATIONS.map((v) => 'forwardedFrom.' + v),
  ...USER_RELATIONS.map((v) => 'parent.from.' + v),
  ...USER_RELATIONS.map((v) => 'parent.to.' + v),
  ...CHAT_RELATIONS.map((v) => 'parent.chat.' + v),
  ...USER_RELATIONS.map((v) => 'parent.forwardedFrom.' + v),
];

export const MESSAGE_RELATIONS = [
  ...LOCALE_MESSAGE_RELATIONS,
  ...LOCALE_MESSAGE_RELATIONS.map((v) => 'parent.' + v),
];

export const CONTACT_RELATIONS = [
  'user',
  'contact',
  ...USER_RELATIONS.map((v) => 'user.' + v),
  ...USER_RELATIONS.map((v) => 'contact.' + v),
];
