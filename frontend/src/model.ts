
export enum VoteValue {
    noVote = 'NO',
    noIdea = '?',
    hidden = 'x',
    small = 'S',
    medium = 'M',
    large = 'L',
    pending = '',
}

export const voteValues = ['S', 'M', 'L'];

export interface UserStatus {
    id: string;
    vote: VoteValue;
}